package co.nos.noswallet.ui.home;

import android.util.Log;

import com.google.gson.JsonElement;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.network.nosModel.AccountHistory;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.adapter.AccountHistoryModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static co.nos.noswallet.network.websockets.WebsocketMachine.safeCast;

public class HomePresenter extends BasePresenter<HomeView> {

    public static final String TAG = HomePresenter.class.getSimpleName();

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    private final NeuroWallet nosWallet = NOSApplication.getNosWallet();

    @Inject
    public HomePresenter() {

    }

    public void requestUpdateHistory() {

    }

    public void requestAccountBalanceCheck() {

    }

    public void onStart() {
        this.requestUpdateHistory();
        this.requestAccountBalanceCheck();
    }


    private Disposable getHistoryDisposable = null;

    public void observeUiCallbacks(WebsocketMachine websocketMachine) {
        if (getHistoryDisposable != null) {
            getHistoryDisposable.dispose();
        }
        getHistoryDisposable = websocketMachine.observeUiTriggers()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WebsocketMachine.SocketResponse>() {
                    @Override
                    public void accept(WebsocketMachine.SocketResponse response) throws Exception {
                        Log.w(TAG, "observeUiCallbacks: onNext: " + response);
                        if (response.isHistoryResponse()) {
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
                        } else if (response.isAccountInformationResponse()) {
                            Log.w(TAG, "got account information response");
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
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: ", throwable);
                    }
                });
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
