package co.nos.noswallet.ui.home;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.nosModel.SocketResponse;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.adapter.AccountHistoryModel;
import co.nos.noswallet.util.S;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static co.nos.noswallet.network.websockets.SafeCast.safeCast;


public class HomePresenter extends BasePresenter<HomeView> {

    public static final String TAG = HomePresenter.class.getSimpleName();

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private final SharedPreferencesUtil sharedPreferencesUtil;
    private final CryptoCurrencyFormatter currencyFormatter;

    public static final String ACCOUNT_HISTORY = "ACCOUNT_HISTORY";
    public static final String ACCOUNT_INFO = "ACCOUNT_INFO";

    @Inject
    public HomePresenter(SharedPreferencesUtil sharedPreferencesUtil,
                         CryptoCurrencyFormatter currencyFormatter) {
        this.sharedPreferencesUtil = sharedPreferencesUtil;
        this.currencyFormatter = currencyFormatter;
    }

    @Nullable
    private SocketResponse getCachedHistoryResponse() {
        return getCachedSocketResponse(ACCOUNT_HISTORY);
    }

    @Nullable
    private SocketResponse getCachedAccountInfoResponse() {
        return getCachedSocketResponse(ACCOUNT_INFO);
    }

    @Nullable
    private SocketResponse getCachedSocketResponse(String key) {
        String json = sharedPreferencesUtil.get(key, null);
        if (json != null) {
            return safeCast(json, SocketResponse.class);
        }
        return null;
    }

    public void requestUpdateHistory(Activity activity) {
        SocketResponse historyResponse = getCachedHistoryResponse();
        if (historyResponse != null && historyResponse.response != null) {
            renderHistoryResponse(historyResponse);
        }

        WebsocketMachine machine = WebsocketMachine.obtain(activity);
        if (machine != null) {
            machine.requestAccountHistoryForAll();
        }
    }

    public void doOnResume(Activity activity) {
        observeUiCallbacks(activity);
    }

    private Disposable uiCallbacksDisposable = null;

    public void observeUiCallbacks(Activity activity) {
        if (uiCallbacksDisposable != null) {
            uiCallbacksDisposable.dispose();
        }
        WebsocketMachine machine = WebsocketMachine.obtain(activity);
        if (machine != null) {
            uiCallbacksDisposable = machine.observeUiTriggers()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        Log.w(TAG, "observeUiCallbacks: onNext: " + response);
                        if (response.isHistoryResponse()) {
                            renderHistoryResponse(response);
                        } else if (response.isAccountInformationResponse()) {
                            renderAccountInfoResponse(response);
                        }
                    }, this::handleUiErrors);
        }
    }

    private void handleUiErrors(Throwable throwable) {
        Log.e(TAG, "handleUiErrors: ", throwable);
        throwable.printStackTrace();
    }

    private void renderAccountInfoResponse(SocketResponse response) {

    }

    private void renderHistoryResponse(SocketResponse response) {
        if (view.isNotAttached()) return;

        sharedPreferencesUtil.set(ACCOUNT_HISTORY, S.GSON.toJson(response));
        JsonElement element = response.response;

        Log.d(TAG, "renderHistoryResponse() called with: response = [" + response + "]");

        if (element != null && element.isJsonObject()) {
            JsonObject o = element.getAsJsonObject();
            if (o.has("history")) {
                JsonElement historyElement = o.get("history");
                if (historyElement != null) {
                    if (historyElement.isJsonArray()) {
                        JsonArray array = historyElement.getAsJsonArray();
                        ArrayList<AccountHistory> entries = new ArrayList<>();
                        for (JsonElement jsonElement : array) {
                            AccountHistory accountHistory = safeCast(jsonElement, AccountHistory.class);
                            if (accountHistory != null) {
                                entries.add(accountHistory);
                            }
                        }
                        view.showHistory(entries);

                    } else if (historyElement.isJsonObject()) {
                        AccountHistory account = safeCast(historyElement.getAsJsonObject(), AccountHistory.class);
                        view.showHistory(new ArrayList<AccountHistory>() {{
                            add(account);
                        }});
                    }
                }
            }
        }
    }

    public void resumePendingTransactions(Activity activity) {
        WebsocketMachine machine = WebsocketMachine.obtain(activity);
        if (machine != null) machine.resumePendingTransactions();
    }
}
