package co.nos.noswallet.network.websockets;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.nosModel.GetBlocksResponse;
import co.nos.noswallet.network.nosModel.SocketResponse;
import co.nos.noswallet.network.notifications.NosNotifier;
import co.nos.noswallet.network.websockets.model.PendingBlocksCredentialsBag;
import co.nos.noswallet.network.websockets.model.PendingSendCoinsCredentialsBag;
import co.nos.noswallet.network.websockets.model.WebSocketsState;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static co.nos.noswallet.network.websockets.SafeCast.safeCast;
import static co.nos.noswallet.network.websockets.SafeCast.safeGetOr;
import static co.nos.noswallet.network.websockets.model.WebSocketsState.GET_ACCOUNT_INFO;
import static co.nos.noswallet.network.websockets.model.WebSocketsState.GET_PENDING_BLOCKS;
import static co.nos.noswallet.network.websockets.model.WebSocketsState.TRANSFER_COINS_PROCESS_WORK;

public class CurrencyHandler {

    public static final String TAG = CurrencyHandler.class.getSimpleName();
    private final long TIMEOUT = 15_000;

    private CryptoCurrency currency;
    private RequestInventor requestInventor;
    private RepresentativesProvider representativesProvider;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final BehaviorSubject<SocketResponse> uiResponses = BehaviorSubject.create();

    private AtomicReference<WebSocketsState> currentState = new AtomicReference<>(WebSocketsState.IDLE);
    private AtomicReference<PendingBlocksCredentialsBag> pendingBlocksBag = new AtomicReference<>(new PendingBlocksCredentialsBag());
    private AtomicReference<PendingSendCoinsCredentialsBag> pendingSendCoinsBag = new AtomicReference<>(new PendingSendCoinsCredentialsBag());
    private AtomicBoolean accountHistoryRequested = new AtomicBoolean(false);
    private AtomicBoolean accountInfoRequested = new AtomicBoolean(false);

    public volatile String recentAccountBalance;

    @Nullable
    private WebsocketExecutor websocketExecutor;

    private final Runnable triggerGetAccountHistory = () -> {
        if (websocketExecutor != null && requestInventor != null) {
            websocketExecutor.send(requestInventor.getAccountHistory(currency));
        }
    };

    public CurrencyHandler(CryptoCurrency currency,
                           RequestInventor requestInventor,
                           RepresentativesProvider representativesProvider) {
        this.currency = currency;
        this.representativesProvider = representativesProvider;
        this.requestInventor = requestInventor;
    }

    public boolean currencyMatches(String currencyCode) {
        return this.currency.getCurrencyCode().equalsIgnoreCase(currencyCode);
    }

    public boolean currencyMatches(CryptoCurrency currency) {
        return this.currency == currency;
    }

    public void handle(SocketResponse response, @Nullable WebsocketExecutor websocketExecutor) {
        this.websocketExecutor = websocketExecutor;
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
        if (websocketExecutor == null) {
            return;
        }
        System.out.println("current pending blocks state: " + currentState.get());
        switch (currentState.get()) {
            case IDLE:
                break;
            case GET_ACCOUNT_HISTORY:
                Log.i(TAG, "got " + currentState.get().name() + ". do nothing");
                websocketExecutor.send(requestInventor.getAccountHistory(currency));
                pushState(WebSocketsState.IDLE);
                break;
            case GET_PENDING_BLOCKS:
                websocketExecutor.send(requestInventor.getPendingBlocks(currency));
                pushState(WebSocketsState.IDLE);
                break;
            case GET_ACCOUNT_INFO:
                websocketExecutor.send(requestInventor.getAccountInformation(currency));
                pushState(WebSocketsState.IDLE);
                break;
            case GENERATE_WORK:
                websocketExecutor.send(requestInventor.generateWork(pendingBlocksBag.get().frontier, currency));
                pushState(WebSocketsState.IDLE);
                break;
            case PROCESS_WORK:
                websocketExecutor.send(requestInventor.processBlock(pendingBlocksBag.get(), currency));
                pushState(WebSocketsState.IDLE);
                break;
            case TRANSFER_COINS_GENERATE_WORK:
                websocketExecutor.send(requestInventor.generateWork(pendingSendCoinsBag.get().frontier, currency));
                pushState(TRANSFER_COINS_PROCESS_WORK);
                break;
            case TRANSFER_COINS_PROCESS_WORK:
                websocketExecutor.send(requestInventor.processSendCoinsBlock(pendingSendCoinsBag.get(), currency));
                pushState(WebSocketsState.GET_ACCOUNT_HISTORY);
                break;
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
            pushState(GET_PENDING_BLOCKS);
        }
    }

    private static String[] invalidJsons = new String[]{
            "{\"blocks\":\"[]\"}",
            "{\"blocks\":\"[{}]\"}",
            "{\"blocks\":[{}]}",
            "{\"blocks\":\"[\\\"\\\"]\"}"
    };

    static class StringBlocksResponse {

        @SerializedName("blocks")
        public String blocks;

        public StringBlocksResponse() {
        }
    }

    static class BlocksListCollection extends ArrayList<GetBlocksResponse.BlocksValue> {

    }

    private void processGetPendingBlocksResponse(SocketResponse socketResponse) {
        System.out.println("processGetPendingBlocksResponse: " + socketResponse.response);
        String json = String.valueOf(socketResponse.response);
        for (String invalidJson : invalidJsons) {
            if (invalidJson.equals(json)) {
                Log.e(TAG, "get pending blocks response has no blocks to process");
                noMoreBlockToProcess();
                return;
            }
        }
        JsonElement element = socketResponse.response;
        if (element != null) {
            if (element.isJsonObject()) {
                StringBlocksResponse e = safeCast(element, StringBlocksResponse.class);
                if (e != null) {
                    String blocksAsString = e.blocks;
                    Log.w(TAG, "xddd: " + blocksAsString);
                    BlocksListCollection array = safeCast(blocksAsString, BlocksListCollection.class);
                    GetBlocksResponse response = new GetBlocksResponse(array);
                    boolean result = handleSingleBlockResponse(response);
                    if (result) return;
                }
            }

            GetBlocksResponse res = safeCast(element, GetBlocksResponse.class);
            if (res != null) {
                boolean result = handleSingleBlockResponse(res);
                if (result) return;
            }
        }
        noMoreBlockToProcess();
    }

    private boolean handleSingleBlockResponse(GetBlocksResponse res) {

        Log.e(TAG, "handleSingleBlockResponse: called with " + res);

        if (res.hasBlock() && !res.blocksValueInvalid()) {

            GetBlocksResponse.BlocksValue value = res.getBlock();
            if (value != null) {
                String blockHash = value.hash;

                pushBagState(state -> state
                        .balance(res.getBalance())
                        .blockHash(blockHash)
                        .amount(res.getBlock().amount)
                );
                pushState(WebSocketsState.GET_ACCOUNT_INFO);

                return true;
            }
        }
        return false;
    }

    private void processGetAccountInformationResponse(SocketResponse response) {
        Log.w(TAG, "processGetAccountInformationResponse: " + response.toString());

        requestInventor.setRepresentative(representativesProvider.provideRepresentative(currency), currency);

        String _accountBalance = "0";
        String _frontier = requestInventor.providePublicKey();
        String _previousBlock = "0";

        if (response.response != null) {
            final JsonElement element = response.response;
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                String representative = safeGetOr(object, "representative", null);
                if (representative != null) {
                    requestInventor.setRepresentative(representative, currency);
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
        recentAccountBalance = accountBalance;
        requestInventor.setAccountBalance(accountBalance, currency);
        final String frontier = _frontier;
        requestInventor.setAccountFrontier(frontier, currency);
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


    private void processPublishBlock(SocketResponse response) {
        Log.w(TAG, "processPublishBlock: " + response.toString());
        schedulePendingBlocksAfter(TIMEOUT);
        uiResponses.onNext(response);
        if (response.error == null) {
            //success
            NosNotifier.showNewIncomingTransfer();
        }
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

    private void noMoreBlockToProcess() {
        accountInfoRequested.set(true);
        pushState(GET_ACCOUNT_INFO);
        schedulePendingBlocksAfter(TIMEOUT);
    }

    private void pushState(WebSocketsState state) {
        Log.w(TAG, "pushState: " + state.name() + " for " + currency.name());
        currentState.set(state);
    }

    private void schedulePendingBlocksAfter(long timeout) {
        handler.removeCallbacks(triggerGetAccountHistory);
        handler.postDelayed(triggerGetAccountHistory, timeout);
    }

    private void pushBagState(WebsocketMachine.Mutator mutator) {
        final PendingBlocksCredentialsBag oldState = pendingBlocksBag.get();
        final PendingBlocksCredentialsBag newState = new PendingBlocksCredentialsBag(oldState);
        pendingBlocksBag.set(mutator.mutate(newState));
    }

    public void transferCoins(String sendAmount, String destinationAccount, CryptoCurrency cryptoCurrency) {
        Log.w(TAG, "transferCoins: " + sendAmount + ", " + destinationAccount);
        //todo: generate work
//        websocketExecutor.send();
        String accountNumber = requestInventor.getAccountNumber(cryptoCurrency);

        PendingSendCoinsCredentialsBag bag = pendingSendCoinsBag.get().clear()
                .withPublicKey(requestInventor.getPublicKey())
                .withAccountNumber(accountNumber)
                .withAmount(sendAmount)
                .withPrivateKey(requestInventor.getPrivateKey())
                .withRepresentative(requestInventor.getRepresentative(currency))
                .withAccountBalance(requestInventor.getAccountBalance(currency))
                .withDestinationAccount(destinationAccount)
                .withFrontier(requestInventor.getAccountFrontier(currency));

        pendingSendCoinsBag.set(bag);
        pushState(WebSocketsState.TRANSFER_COINS_GENERATE_WORK);
        handle(null, websocketExecutor);
    }

    public void getAccountHistory() {
        triggerGetAccountHistory.run();
    }

    public Observable<SocketResponse> observeUiTriggers() {
        return uiResponses;
    }

    public void requestAccountHistory() {
        if (websocketExecutor != null) {
            websocketExecutor.send(requestInventor.getAccountHistory(currency));
            accountHistoryRequested.set(true);
        } else {
            Log.e(TAG, "requestAccountInfo: not connected yet!");
        }
    }

    public void requestAccountInfo() {
        if (websocketExecutor != null) {
            websocketExecutor.send(requestInventor.getAccountInformation(currency));
            accountInfoRequested.set(true);
        } else {
            Log.e(TAG, "requestAccountInfo: not connected yet!");
        }
    }

    public void closeConnection() {
        uiResponses.onNext(SocketResponse.SocketClosed);
    }
}
