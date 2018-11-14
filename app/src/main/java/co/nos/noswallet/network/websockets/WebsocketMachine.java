package co.nos.noswallet.network.websockets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.net.ssl.SSLException;

import co.nos.noswallet.BuildConfig;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.nosModel.SocketResponse;
import co.nos.noswallet.network.websockets.model.PendingBlocksCredentialsBag;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.HasWebsocketMachine;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

import static co.nos.noswallet.network.notifications.NosNotifier.ACTION_GOT_SAUCE;

public class WebsocketMachine {

    public String getRecentAccountBalanceOf(CryptoCurrency currencyInUse) {
        CurrencyHandler currencyHandler = getMatchingHandler(currencyInUse);
        if (currencyHandler != null) {
            return currencyHandler.recentAccountBalance;
        }
        return "0";
    }

    public void logout() {
        performForAll(CurrencyHandler::unregisterNotifications);
    }

    public interface DoSth {
        void doSomething(CurrencyHandler handler);
    }

    @Nullable
    public static WebsocketMachine obtain(Activity activity) {
        if (activity instanceof HasWebsocketMachine) {
            return ((HasWebsocketMachine) activity).getWebsocketMachine();
        }
        return null;
    }

    private Runnable afterInitRunnable;

    private final Runnable reconnectToApi = this::setupWebSockets;

    public void doAfterInit(Runnable action) {
        afterInitRunnable = action;
    }

    public boolean isConnected() {
        return isConnectedToTheApi && websocketExecutor != null;
    }

    interface Mutator {
        PendingBlocksCredentialsBag mutate(PendingBlocksCredentialsBag ref);
    }

    private volatile boolean isConnectedToTheApi = false;

    private RepresentativesProvider representativesProvider;

    public static final String TAG = WebsocketMachine.class.getSimpleName();

    private RequestInventor requestInventor;

    @Nullable
    private volatile WebsocketExecutor websocketExecutor;

    private Handler handler = new Handler(Looper.getMainLooper());

    private CompositeDisposable disposable = new CompositeDisposable();

    private final List<CurrencyHandler> currencyHandlers;

    @Inject
    WebsocketMachine(RequestInventor requestInventor,
                     RepresentativesProvider representativesProvider) {
        this.requestInventor = requestInventor;
        this.representativesProvider = representativesProvider;
        this.currencyHandlers = Observable.fromArray(CryptoCurrency.values())
                .map(this::createWithCurrency)
                .toList()
                .blockingGet();
    }

    private CurrencyHandler createWithCurrency(CryptoCurrency cryptoCurrency) {
        return new CurrencyHandler(cryptoCurrency, requestInventor, representativesProvider);
    }

    public void start() {
        setupWebSockets();
    }

    public void pausePendingTransactions() {
        handler.removeCallbacksAndMessages(null);
    }

    public void resumePendingTransactions() {
        for (CurrencyHandler h : currencyHandlers) {
            h.getAccountHistory();
        }
    }

    private void setupWebSockets() {
        Log.i(TAG, "setupWebSockets() called");
        String url = BuildConfig.WEBSOCKET_URL;

        websocketExecutor = new WebsocketExecutor(new OkHttpClient(), url, new NosNodeWebSocketListener());
        websocketExecutor.init(webSocket -> {

            for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
                if (websocketExecutor != null) {
                    websocketExecutor.send(requestInventor.getAccountHistory(cryptoCurrency), webSocket);
                }
                performForAll(CurrencyHandler::registerPushNotificationsWhenAvailable);
            }
            isConnectedToTheApi = true;
            if (afterInitRunnable != null) {
                handler.post(afterInitRunnable);
            }
        });

        Disposable disposable = websocketExecutor.observeMessages()
                .subscribeOn(Schedulers.io())
                .subscribe(this::recognize, this::onError);
        this.disposable.add(disposable);
    }

    private void onError(Throwable err) {
        Log.e(TAG, "onError: ", err);
        err.printStackTrace();

        if (isNetworkError(err) || socketClosedError(err)) {
            isConnectedToTheApi = false;

            handler.removeCallbacks(null);
            handler.postDelayed(reconnectToApi, 3_000);

            performForAll(CurrencyHandler::closeConnection);

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
                || err instanceof SSLException;
    }

    @SuppressLint("LogNotTimber")
    private void recognize(String json) {
        Log.i(TAG, "onNext -> \n" + json);
        SocketResponse response = safeCast(json, SocketResponse.class);

        for (CurrencyHandler handler : currencyHandlers) {
            if (handler.currencyMatches(response.currency)) {
                handler.handle(response, websocketExecutor);
            }
        }
    }

    public Observable<SocketResponse> observeUiTriggers() {
        return observeUiTriggers(CryptoCurrency.NOLLAR);
    }

    public Observable<SocketResponse> observeUiTriggers(CryptoCurrency cryptoCurrency) {
        for (CurrencyHandler handler : currencyHandlers) {
            if (handler.currencyMatches(cryptoCurrency)) {
                return handler.observeUiTriggers();
            }
        }
        return Observable.error(new NoSuchElementException("cannot find matching observable for " + cryptoCurrency.name()));
    }

    public void requestAccountHistory(CryptoCurrency cryptoCurrency) {
        Log.w(TAG, "requestAccountHistory: ");
        getMatchingHandlerAndPerform(cryptoCurrency, CurrencyHandler::requestAccountHistory);
    }

    private void performForAll(DoSth doSth) {
        for (CurrencyHandler handler : currencyHandlers) {
            doSth.doSomething(handler);
        }
    }

    private void getMatchingHandlerAndPerform(CryptoCurrency cryptoCurrency, DoSth doSth) {
        for (CurrencyHandler handler : currencyHandlers) {
            if (handler.currencyMatches(cryptoCurrency)) {
                doSth.doSomething(handler);
            }
        }
    }

    @Nullable
    private CurrencyHandler getMatchingHandler(CryptoCurrency cryptoCurrency) {
        for (CurrencyHandler handler : currencyHandlers) {
            if (handler.currencyMatches(cryptoCurrency)) {
                return handler;
            }
        }
        return null;
    }

    public void requestAccountHistoryForAll() {
        performForAll(CurrencyHandler::requestAccountHistory);
    }

    public void requestAccountInfo(CryptoCurrency cryptoCurrency) {
        Log.w(TAG, "requestAccountInfo: ");
        getMatchingHandlerAndPerform(cryptoCurrency, CurrencyHandler::requestAccountInfo);
    }

    public void transferCoins(String sendAmount, String destinationAccount, CryptoCurrency cryptoCurrency) {
        for (CurrencyHandler currencyHandler : currencyHandlers) {
            if (currencyHandler.currencyMatches(cryptoCurrency.getCurrencyCode())) {
                currencyHandler.transferCoins(sendAmount, destinationAccount, cryptoCurrency);
            }
        }
    }

    public void handleClickedNotification(Intent intent) {
        if (ACTION_GOT_SAUCE.equalsIgnoreCase(intent.getAction())){
            performForAll(CurrencyHandler::requestGetPendingBlocks);
        }
    }

    public static <T> T safeCast(String json, Class<T> klazz) {
        return SafeCast.safeCast(json, klazz);
    }

}
