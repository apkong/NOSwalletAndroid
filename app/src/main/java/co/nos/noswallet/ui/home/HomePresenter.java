package co.nos.noswallet.ui.home;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonElement;

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
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.adapter.AccountHistoryModel;
import co.nos.noswallet.util.S;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static co.nos.noswallet.network.websockets.WebsocketMachine.safeCast;

public class HomePresenter extends BasePresenter<HomeView> {

    public static final String TAG = HomePresenter.class.getSimpleName();

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private final SharedPreferencesUtil sharedPreferencesUtil;

    public static final String ACCOUNT_HISTORY = "ACCOUNT_HISTORY";
    public static final String ACCOUNT_INFO = "ACCOUNT_INFO";

    @Inject
    public HomePresenter(SharedPreferencesUtil sharedPreferencesUtil) {
        this.sharedPreferencesUtil = sharedPreferencesUtil;
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
        if (historyResponse != null) {
            renderHistoryResponse(historyResponse);
        }

        WebsocketMachine machine = WebsocketMachine.obtain(activity);
        if (machine != null) {
            machine.requestAccountHistory();
        }
    }

    public void requestCachedResponsesIfAny() {
        requestAccountInfo(null);
        requestUpdateHistory(null);
    }

    public void requestAccountInfo(Activity activity) {
        SocketResponse accountInfoResponse = getCachedAccountInfoResponse();
        if (accountInfoResponse != null) {
            renderHistoryResponse(accountInfoResponse);
        }

        WebsocketMachine machine = WebsocketMachine.obtain(activity);
        if (machine != null) {
            machine.requestAccountInfo();
        }
    }

    public void doOnResume(Activity activity) {
        observeUiCallbacks(activity);
        requestUpdateHistory(activity);
        requestAccountInfo(activity);
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
        Log.w(TAG, "got account information response");
        sharedPreferencesUtil.set(ACCOUNT_INFO, S.GSON.toJson(response));

        JsonElement element = response.response;
        AccountInfoModel model = safeCast(element, AccountInfoModel.class);
        if (model != null) {
            String balance = model.balance;
            String currency = response.currency == null ? CryptoCurrency.NOLLAR.getCurrencyCode() : response.currency;
            if (balance != null) {
                String formattedCurrency = CryptoCurrency.formatWith(currency, balance);
                view.onBalanceFormattedReceived(formattedCurrency);
            }
        }
    }

    private void renderHistoryResponse(SocketResponse response) {
        sharedPreferencesUtil.set(ACCOUNT_HISTORY, S.GSON.toJson(response));
        JsonElement element = response.response;

        if (element.isJsonObject()) {
            AccountHistoryModel model = safeCast(element, AccountHistoryModel.class);
            if (model != null) {
                view.showHistory(new ArrayList<AccountHistory>() {{
                    add(new AccountHistory(model.balance, wellFormattedDate(model)));
                }});
            }
        } else if (element.isJsonArray()) {

        }
    }

    private String wellFormattedDate(AccountHistoryModel model) {
        String timestamp = model.modified_timestamp;
        long asLong = stringToLong(timestamp);
        if (asLong > -1) {
            Date date = new Date(asLong * 1000);

            return dateFormat.format(date);
        } else {
            return "";
        }
    }

    private long stringToLong(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException x) {
            return -1L;
        }
    }
}
