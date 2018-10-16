package co.nos.noswallet.network.websockets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.net.ssl.SSLException;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.nosModel.GetBlocksResponse;
import co.nos.noswallet.network.websockets.model.WebSocketsState;
import co.nos.noswallet.ui.home.HasWebsocketMachine;
import co.nos.noswallet.util.S;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;

import static co.nos.noswallet.network.websockets.model.WebSocketsState.GET_ACCOUNT_INFO;
import static co.nos.noswallet.network.websockets.model.WebSocketsState.TRANSFER_COINS_PROCESS_WORK;

public class WebsocketMachine {

    @Nullable
    public static WebsocketMachine obtain(Activity activity) {
        if (activity instanceof HasWebsocketMachine) {
            return ((HasWebsocketMachine) activity).getWebsocketMachine();
        }
        return null;
    }

    interface Mutator {
        PendingBlocksCredentialsBag mutate(PendingBlocksCredentialsBag ref);
    }

    int retriesBecauseOfError = 0;

    private BehaviorSubject<SocketResponse> uiResponses = BehaviorSubject.create();

    private RepresentativesProvider representativesProvider;

    private final long TIMEOUT = 15_000;

    public static final String TAG = WebsocketMachine.class.getSimpleName();

    private RequestInventor requestInventor;

    private volatile WebsocketExecutor websocketExecutor;

    private Handler handler = new Handler(Looper.getMainLooper());

    private CompositeDisposable disposable = new CompositeDisposable();

    private AtomicReference<WebSocketsState> currentState = new AtomicReference<>(WebSocketsState.IDLE);
    private AtomicReference<PendingBlocksCredentialsBag> pendingBlocksBag = new AtomicReference<>(new PendingBlocksCredentialsBag());
    private AtomicReference<PendingSendCoinsCredentialsBag> pendingSendCoinsBag = new AtomicReference<>(new PendingSendCoinsCredentialsBag());
    private AtomicBoolean accountHistoryRequested = new AtomicBoolean(false);
    private AtomicBoolean accountInfoRequested = new AtomicBoolean(false);
    public volatile String recentAccountBalance;

    private final Runnable reconnectToApi = this::setupWebSockets;

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
        handler.removeCallbacksAndMessages(null);
    }

    private void setupWebSockets() {
        Log.i(TAG, "setupWebSockets() called");
        String url = BuildConfig.WEBSOCKET_URL;

        websocketExecutor = new WebsocketExecutor(new OkHttpClient(),
                url, new NosNodeWebSocketListener());

        websocketExecutor.init(webSocket -> websocketExecutor.send(requestInventor.getAccountHistory(), webSocket));

        Disposable disposable = websocketExecutor.observeMessages()
                .subscribeOn(Schedulers.io())
                .subscribe(this::recognize, this::onError);
        this.disposable.add(disposable);
    }

    private void onError(Throwable err) {
        Log.e(TAG, "onError: ", err);
        err.printStackTrace();
//        if (isNetworkError(err)) {
//            //no internet here, retry after 3 seconds
//            handler.removeCallbacksAndMessages(null);
//            if (retriesBecauseOfError < 4) {
//                ++retriesBecauseOfError;
//                handler.removeCallbacks(reconnectToApi);
//                handler.postDelayed(reconnectToApi, 3_000);
//            } else {
//                Log.w(TAG, "onError: reached timeouts limit ");
//            }
//        }

        if (isNetworkError(err)||socketClosedError(err)) {
            handler.removeCallbacks(null);
            handler.postDelayed(reconnectToApi, 3_000);
        } else {
            Log.e(TAG, "onError: unrecognized error", err);
        }
    }

    private boolean socketClosedError(Throwable err) {
        return err instanceof EOFException;
    }

    private boolean isNetworkError(Throwable err) {
        return err instanceof SocketTimeoutException
                || err instanceof UnknownHostException
                || err instanceof SSLException
                || err instanceof SocketTimeoutException;
    }


    @SuppressLint("LogNotTimber")
    private void recognize(String json) {
        Log.i(TAG, "onNext -> \n" + json);
        SocketResponse response = safeCast(json, SocketResponse.class);
        if (response != null) {
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
        }
        System.out.println("current pending blocks state: " + currentState.get());
        switch (currentState.get()) {
            case IDLE:
                break;
            case GET_ACCOUNT_HISTORY:
                Log.i(TAG, "got " + currentState.get().name() + ". do nothing");
                websocketExecutor.send(requestInventor.getAccountHistory());
                pushState(WebSocketsState.IDLE);
                break;
            case GET_PENDING_BLOCKS:
                websocketExecutor.send(requestInventor.getPendingBlocks());
                pushState(WebSocketsState.IDLE);
                break;
            case GET_ACCOUNT_INFO:
                websocketExecutor.send(requestInventor.getAccountInformation());
                pushState(WebSocketsState.IDLE);
                break;
            case GENERATE_WORK:
                websocketExecutor.send(requestInventor.generateWork(
                        pendingBlocksBag.get().frontier
                ));
                pushState(WebSocketsState.IDLE);
                break;
            case PROCESS_WORK:
                websocketExecutor.send(requestInventor.processBlock(pendingBlocksBag.get()));
                pushState(WebSocketsState.IDLE);
                break;
            case TRANSFER_COINS_GENERATE_WORK:
                websocketExecutor.send(requestInventor.generateWork(pendingSendCoinsBag.get().frontier));
                pushState(TRANSFER_COINS_PROCESS_WORK);
                break;
            case TRANSFER_COINS_PROCESS_WORK:
                websocketExecutor.send(requestInventor.processSendCoinsBlock(pendingSendCoinsBag.get()));
                pushState(WebSocketsState.GET_ACCOUNT_HISTORY);
                break;
        }
    }

    private void processPublishBlock(SocketResponse response) {
        Log.w(TAG, "processPublishBlock: " + response.toString());
        schedulePendingBlocksAfter(TIMEOUT);
        uiResponses.onNext(response);
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

                    if (currentState.get() == TRANSFER_COINS_PROCESS_WORK) {
                        PendingSendCoinsCredentialsBag bag = pendingSendCoinsBag.get();
                        pendingSendCoinsBag.set(new PendingSendCoinsCredentialsBag(bag).withProofOfWork(work));
                    } else {
                        Log.i(TAG, "got proof of work " + work);
                        pushBagState(ref -> ref.proofOfWork(work));
                        pushState(WebSocketsState.PROCESS_WORK);
                    }
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
                _frontier = safeGetOr(object, "frontier_block_hash", _frontier);
                if (!_accountBalance.equals("0")) {
                    _previousBlock = _frontier;
                } else {
                    _previousBlock = "0";
                }
            }
            uiResponses.onNext(response);
        }

        final String previousBlock = _previousBlock;
        final String accountBalance = _accountBalance;
        recentAccountBalance = (accountBalance);
        requestInventor.setAccountBalance(accountBalance);
        final String frontier = _frontier;
        requestInventor.setAccountFrontier(frontier);
        pushBagState(ref -> ref.previousBlock(previousBlock)
                .accountBalance(accountBalance)
                .frontier(frontier)
        );
        if (accountInfoRequested.get()) {
            accountInfoRequested.set(false);
            pushState(WebSocketsState.IDLE);
        } else {
            pushState(WebSocketsState.GENERATE_WORK);
        }
    }

    private void processGetAccountHistory(SocketResponse response) {
        System.out.println();
        if (response.error != null) {
            System.out.println(response.error);
        } else {
            System.out.println(response.response);
            uiResponses.onNext(response);
        }
        if (accountHistoryRequested.get()) {
            accountHistoryRequested.set(false);
            pushState(WebSocketsState.IDLE);
        } else {
            pushState(WebSocketsState.GET_PENDING_BLOCKS);
        }
    }

    private void processGetPendingBlocksResponse(SocketResponse socketResponse) {
        System.out.println("processGetPendingBlocksResponse: " + socketResponse.response);
        if (socketResponse.error != null) {
            System.out.println(socketResponse.error);
            noMoreBlockToProcess();
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
                noMoreBlockToProcess();
            }
        }
    }

    private void noMoreBlockToProcess() {
        accountInfoRequested.set(true);
        pushState(GET_ACCOUNT_INFO);
        schedulePendingBlocksAfter(TIMEOUT);
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

    public void requestAccountHistory() {
        Log.w(TAG, "requestAccountHistory: ");
        websocketExecutor.send(requestInventor.getAccountHistory());
        accountHistoryRequested.set(true);
    }

    public void requestAccountInfo() {
        Log.w(TAG, "requestAccountInfo: ");
        websocketExecutor.send(requestInventor.getAccountInformation());
        accountInfoRequested.set(true);
    }


    public void transferCoins(String sendAmount, String destinationAccount) {
        Log.w(TAG, "transferCoins: " + sendAmount + ", " + destinationAccount);
        //todo: generate work
//        websocketExecutor.send();
        String accountNumber = requestInventor.getAccountNumber();

        PendingSendCoinsCredentialsBag bag = pendingSendCoinsBag.get().clear()
                .withPublicKey(requestInventor.getPublicKey())
                .withAccountNumber(accountNumber)
                .withAmount(sendAmount)
                .withPrivateKey(requestInventor.getPrivateKey())
                .withRepresentative(requestInventor.getRepresentative())
                .withAccountBalance(requestInventor.getAccountBalance())
                .withDestinationAccount(destinationAccount)
                .withFrontier(requestInventor.getAccountFrontier());

        pendingSendCoinsBag.set(bag);
        pushState(WebSocketsState.TRANSFER_COINS_GENERATE_WORK);
        recognize(null);
    }


    public static <T> T safeCast(JsonElement json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            Log.e(TAG, "safeCast: ", x);
            return null;
        }
    }

    public static <T> T safeCast(String json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            return null;
        }
    }

    public static String safeGet(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return null;
        return e.getAsString();
    }

    public static String safeGetOr(JsonObject o, String key, String defaultValue) {
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

        public boolean isAccountInformationResponse() {
            return "get_account_information".equals(action);
        }

        public boolean isProcessedBlock() {
            return "publish_block".equals(action);
        }
    }

    public static class PendingSendCoinsCredentialsBag {
        public String accountNumber, publicKey, amount;
        public String representative, privateKey, frontier;
        public String accountBalance, destinationAccount, work;

        @Override
        public String toString() {
            return "{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", publicKey='" + publicKey + '\'' +
                    ", amount='" + amount + '\'' +
                    ", representative='" + representative + '\'' +
                    ", privateKey='" + privateKey + '\'' +
                    ", frontier='" + frontier + '\'' +
                    ", accountBalance='" + accountBalance + '\'' +
                    ", destinationAccount='" + destinationAccount + '\'' +
                    ", work='" + work + '\'' +
                    '}';
        }

        public PendingSendCoinsCredentialsBag() {

        }

        private PendingSendCoinsCredentialsBag(PendingSendCoinsCredentialsBag previousBag) {
            accountNumber = previousBag.accountNumber;
            publicKey = previousBag.publicKey;
            amount = previousBag.amount;
            representative = previousBag.representative;
            privateKey = previousBag.privateKey;
            frontier = previousBag.frontier;
            destinationAccount = previousBag.destinationAccount;
            accountBalance = previousBag.accountBalance;
            work = previousBag.work;
        }

        public PendingSendCoinsCredentialsBag withAccountNumber(String val) {
            accountNumber = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withPublicKey(String val) {
            publicKey = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withAmount(String val) {
            amount = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withDestinationAccount(String val) {
            destinationAccount = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withRepresentative(String val) {
            representative = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withPrivateKey(String val) {
            privateKey = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withFrontier(String val) {
            frontier = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withAccountBalance(String val) {
            accountBalance = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag withProofOfWork(String val) {
            work = val;
            return this;
        }

        public PendingSendCoinsCredentialsBag clear() {
            accountNumber
                    = publicKey
                    = amount
                    = representative
                    = privateKey
                    = frontier
                    = accountBalance
                    = work
                    = destinationAccount = null;
            return this;
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
