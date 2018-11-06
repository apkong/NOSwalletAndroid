package co.nos.noswallet.ui.home.v2;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.nosModel.SocketResponse;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.AccountInfoModel;
import co.nos.noswallet.util.S;
import co.nos.noswallet.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;

import static co.nos.noswallet.network.websockets.SafeCast.safeCast;

public class CurrencyPresenter {

    public static final String TAG = CurrencyPresenter.class.getSimpleName();
    public static final String ACCOUNT_HISTORY = "ACCOUNT_HISTORY";
    public static final String ACCOUNT_INFO = "ACCOUNT_INFO";

    private CurrencyView view;

    private SerialDisposable serialDisposable = new SerialDisposable();

    private CryptoCurrencyFormatter currencyFormatter;
    private final SharedPreferencesUtil sharedPreferencesUtil;

    public CurrencyPresenter(SharedPreferencesUtil sharedPreferencesUtil) {
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    public void setCurrencyFormatter(CryptoCurrencyFormatter currencyFormatter) {
        this.currencyFormatter = currencyFormatter;
    }

    public void attachView(CurrencyView view) {
        this.view = view;
    }

    public void resume(WebsocketMachine machine, CryptoCurrency cryptoCurrency) {
        machine.requestAccountHistory(cryptoCurrency);
        machine.requestAccountInfo(cryptoCurrency);
        serialDisposable.set(machine.observeUiTriggers(cryptoCurrency)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isHistoryResponse()) {
                        renderHistoryResponse(response, cryptoCurrency);
                    } else if (response.isAccountInformationResponse()) {
                        renderAccountInfoResponse(response, cryptoCurrency);
                    } else {
                        Log.w(TAG, "showing socket response: " + response);
                    }
                }, throwable -> {
                    Log.e(TAG, "CurrencyPresenter: ", throwable);
                    throwable.printStackTrace();
                })
        );
    }

    private void renderAccountInfoResponse(SocketResponse response, CryptoCurrency cryptoCurrency) {
        Log.w(TAG, "got account information response: " + response);
        sharedPreferencesUtil.set(ACCOUNT_INFO + cryptoCurrency.name(), S.GSON.toJson(response));

        JsonElement element = response.response;
        AccountInfoModel model = safeCast(element, AccountInfoModel.class);
        if (model != null) {
            String balance = model.balance;
            String currency = response.currency == null ? CryptoCurrency.NOLLAR.getCurrencyCode() : response.currency;
            if (balance != null) {
                String uiBalance = currencyFormatter.rawtoUi(balance);
                String formattedCurrency = CryptoCurrency.formatWith(currency, uiBalance);
                view.onBalanceFormattedReceived(formattedCurrency);
                return;
            }
        }
        view.onBalanceFormattedReceived("??? " + cryptoCurrency.name());
    }

    private void renderHistoryResponse(SocketResponse response, CryptoCurrency cryptoCurrency) {
        if (view.isNotAttached()) return;

        sharedPreferencesUtil.set(ACCOUNT_HISTORY + cryptoCurrency.name(), S.GSON.toJson(response));
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
                        return;

                    } else if (historyElement.isJsonObject()) {
                        AccountHistory account = safeCast(historyElement.getAsJsonObject(), AccountHistory.class);
                        view.showHistory(new ArrayList<AccountHistory>() {{
                            add(account);
                        }});
                        return;
                    }
                }
            }
        }
        view.showHistory(new ArrayList<>());
    }
}
