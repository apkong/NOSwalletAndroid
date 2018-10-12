package co.nos.noswallet.network.websockets;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.nosModel.GetBlocksResponse;
import co.nos.noswallet.network.websockets.model.WebSocketsState;
import co.nos.noswallet.util.S;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;

public class WebsocketMachine {

    interface Mutator {
        PendingBlocksCredentialsBag mutate(PendingBlocksCredentialsBag ref);
    }

    private BehaviorSubject<SocketResponse> uiResponses = BehaviorSubject.create();

    private RepresentativesProvider representativesProvider;

    private final long TIMEOUT = 15_000;

    public static final String TAG = WebsocketMachine.class.getSimpleName();

    private RequestInventor requestInventor;

    private volatile WebsocketExecutor websocketExecutor;

    private Handler handler = new Handler();

    private CompositeDisposable disposable = new CompositeDisposable();

    private AtomicReference<WebSocketsState> currentState = new AtomicReference<>(WebSocketsState.NONE);
    private AtomicReference<PendingBlocksCredentialsBag> pendingBlocksBag =
            new AtomicReference<>(new PendingBlocksCredentialsBag());

    private final Runnable triggerGetAccountHistory = () -> {
        if (websocketExecutor != null && requestInventor != null) {
            websocketExecutor.send(requestInventor.getAccountHistory());
        }
    };

    @Inject
    WebsocketMachine(RequestInventor requestInventor,
                     RepresentativesProvider representativesProvider) {
        this.requestInventor = requestInventor;
        this.representativesProvider = representativesProvider;
    }

    public void start() {
        setupWebSockets();
    }

    public void pause() {
        disposable.clear();
    }

    private void setupWebSockets() {
        Log.i(TAG, "setupWebSockets() called");
        String url = BuildConfig.WEBSOCKET_URL;

        websocketExecutor = new WebsocketExecutor(new OkHttpClient(),
                url, new NosNodeWebSocketListener());

        websocketExecutor.init(
                (websocket) ->
                        //fetch some representative at the first time
                        websocketExecutor.send(requestInventor.getAccountHistory(), websocket)
        );

        Disposable disposable = websocketExecutor.observeMessages()
                .subscribeOn(Schedulers.io())
                .subscribe(this::recognize, this::onError);
        this.disposable.add(disposable);
    }

    private void onError(Throwable err) {
        Log.e(TAG, "onError: ", err);
        err.printStackTrace();
        if (err instanceof UnknownHostException) {
            //no internet here, retry after 3 seconds
            handler.removeCallbacksAndMessages(null);
            if (retries < 4) {
                ++retries;
                handler.postDelayed(this::setupWebSockets, 3_000);
            }
        }
    }

    int retries = 0;

    @SuppressLint("LogNotTimber")
    private void recognize(String json) {
        Log.i(TAG, "onNext -> \n" + json);
        SocketResponse response = S.GSON.fromJson(json, SocketResponse.class);
        switch (String.valueOf(response.action).toLowerCase()) {
            case "get_account_history": {
                processGetAccountHistory(response);
                break;
            }
            case "get_pending_blocks": {
                processGetPendingBlocksResponse(response);
                break;
            }
            case "get_account_information": {
                processGetAccountInformationResponse(response);
                break;
            }
            case "get_pow": {
                processGenerateWorkResponse(response);
                break;
            }
            case "publish_block": {
                processPublishBlock(response);
                break;
            }
        }
        System.out.println("current pending blocks state: " + currentState.get());
        switch (currentState.get()) {
            case NONE:
                break;
            case GET_ACCOUNT_HISTORY:
                Log.i(TAG, "got " + currentState.get().name() + ". do nothing");
                websocketExecutor.send(requestInventor.getAccountHistory());
                break;
            case GET_PENDING_BLOCKS:
                websocketExecutor.send(requestInventor.getPendingBlocks());
                break;
            case GET_ACCOUNT_INFO:
                websocketExecutor.send(requestInventor.getAccountInformation());
                break;
            case GENERATE_WORK:
                websocketExecutor.send(requestInventor.generateWork(
                        pendingBlocksBag.get().frontier
                ));
                break;
            case PROCESS_WORK:
                websocketExecutor.send(requestInventor.processBlock(pendingBlocksBag.get()));
                break;
        }
        pushState(WebSocketsState.NONE);
    }

    private void processPublishBlock(SocketResponse response) {
        Log.w(TAG, "processPublishBlock: " + response.toString());
        schedulePendingBlocksAfter(TIMEOUT);
    }

    private void processGenerateWorkResponse(SocketResponse response) {
        Log.e(TAG, "processGenerateWorkResponse: " + response.toString());
        if (response.error != null) {
            System.err.println(response.error);
        } else {
            JsonElement element = response.response;
            if (element.isJsonObject()) {
                JsonObject o = element.getAsJsonObject();
                if (o.has("work")) {
                    String work = o.get("work").getAsString();
                    Log.i(TAG, "got proof of work " + work);
                    pushBagState(ref -> ref.proofOfWork(work));
                    pushState(WebSocketsState.PROCESS_WORK);
                }
            }
        }
    }

    private void processGetAccountInformationResponse(SocketResponse response) {
        Log.w(TAG, "processGetAccountInformationResponse: " + response.toString());

        requestInventor.setRepresentative(representativesProvider.provideRepresentative());

        String _accountBalance = "0";
        String _frontier = requestInventor.providePublicKey();
        String _previousBlock = "0";

        if (response.response != null) {
            final JsonElement element = response.response;
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                String representative = safeGetOr(object, "representative", null);
                if (representative != null) {
                    requestInventor.setRepresentative(representative);
                }

                _accountBalance = safeGetOr(object, "balance", "0");
                _frontier = safeGetOr(object, "frontier", _frontier);
                if (!_accountBalance.equals("0")) {
                    _previousBlock = _frontier;
                } else {
                    _previousBlock = "0";
                }
            }
        }

        final String previousBlock = _previousBlock;
        final String accountBalance = _accountBalance;
        final String frontier = _frontier;
        pushBagState(ref -> ref.previousBlock(previousBlock)
                .accountBalance(accountBalance)
                .frontier(frontier)
        );
        pushState(WebSocketsState.GENERATE_WORK);
    }

    private void processGetAccountHistory(SocketResponse response) {
        System.out.println();
        if (response.error != null) {
            System.out.println(response.error);
        } else {
            System.out.println(response.response);
            uiResponses.onNext(response);

        }
        pushState(WebSocketsState.GET_PENDING_BLOCKS);
    }

    private void processGetPendingBlocksResponse(SocketResponse socketResponse) {
        System.out.println("processGetPendingBlocksResponse: " + socketResponse.response);
        if (socketResponse.error != null) {
            System.out.println(socketResponse.error);
        } else {
            JsonElement blocksElement = socketResponse.response;

            GetBlocksResponse blocksInfoResponse = safeCast(blocksElement, GetBlocksResponse.class);
            if (blocksInfoResponse != null && blocksInfoResponse.hasBlock()) {
                String blockHash = blocksInfoResponse.getBlock().hash;

                pushBagState(state -> state
                        .balance(blocksInfoResponse.getBalance())
                        .blockHash(blockHash)
                        .amount(blocksInfoResponse.getBlock().amount)
                );
                pushState(WebSocketsState.GET_ACCOUNT_INFO);
            } else {
                //no further pending blocks,retry after 15 seconds
                schedulePendingBlocksAfter(TIMEOUT);
            }
        }
    }

    private void pushState(WebSocketsState state) {
        Log.w(TAG, "pushState: " + state.name());
        currentState.set(state);
    }

    private void schedulePendingBlocksAfter(long timeout) {
        handler.removeCallbacks(triggerGetAccountHistory);
        handler.postDelayed(triggerGetAccountHistory, timeout);
    }

    private void pushBagState(Mutator mutator) {
        final PendingBlocksCredentialsBag oldState = pendingBlocksBag.get();
        final PendingBlocksCredentialsBag newState = new PendingBlocksCredentialsBag(oldState);
        pendingBlocksBag.set(mutator.mutate(newState));
    }

    public Observable<SocketResponse> observeUiTriggers() {
        return uiResponses;
    }

    static <T> T safeCast(JsonElement json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            return null;
        }
    }

    static <T> T safeCast(String json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            return null;
        }
    }

    static String safeGet(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return null;
        return e.getAsString();
    }

    static String safeGetOr(JsonObject o, String key, String defaultValue) {
        String value = safeGet(o, key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }


    public static class SocketResponse {

        @SerializedName("action")
        public String action;

        @SerializedName("currency")
        public String currency;

        @SerializedName("error")
        public String error;

        @SerializedName("response")
        public JsonElement response;

        public SocketResponse() {
        }

        @Override
        public String toString() {
            return "SocketResponse{" +
                    "action='" + action + '\'' +
                    ", currency='" + currency + '\'' +
                    ", error='" + error + '\'' +
                    ", response=" + String.valueOf(response) +
                    '}';
        }

        public boolean isHistoryResponse() {
            return "get_account_history".equals(action);
        }
    }

    public static class PendingBlocksCredentialsBag {
        public String balance, amount;
        public String previousBlock, frontier;
        public String accountBalance;
        public String work;
        public String blockHash;

        public PendingBlocksCredentialsBag() {
        }

        public PendingBlocksCredentialsBag(PendingBlocksCredentialsBag previous) {
            this.balance = previous.balance;
            this.amount = previous.amount;
            this.previousBlock = previous.previousBlock;
            this.frontier = previous.frontier;
            this.accountBalance = previous.accountBalance;
            this.work = previous.work;
            this.blockHash = previous.blockHash;
        }

        public PendingBlocksCredentialsBag balance(String val) {
            this.balance = val;
            return this;
        }

        public PendingBlocksCredentialsBag amount(String val) {
            this.amount = val;
            return this;
        }

        public PendingBlocksCredentialsBag previousBlock(String val) {
            this.previousBlock = val;
            return this;
        }

        public PendingBlocksCredentialsBag frontier(String val) {
            this.frontier = val;
            return this;
        }


        public PendingBlocksCredentialsBag accountBalance(String val) {
            this.accountBalance = val;
            return this;
        }

        public PendingBlocksCredentialsBag proofOfWork(String val) {
            this.work = val;
            return this;
        }

        public PendingBlocksCredentialsBag blockHash(String val) {
            this.blockHash = val;
            return this;
        }


        public PendingBlocksCredentialsBag clear() {
            this.balance = null;
            this.amount = null;
            this.frontier = null;
            this.previousBlock = null;
            this.accountBalance = null;
            this.work = null;
            this.blockHash = null;
            return this;
        }
    }
}
