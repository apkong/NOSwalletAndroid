package co.nos.noswallet.ui.send;

import android.util.Log;

import java.math.BigDecimal;

import javax.inject.Inject;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.model.NeuroWallet;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.SerialDisposable;
import io.realm.Realm;

public class SendCoinsPresenter extends BasePresenter<SendCoinsView> {

    public CryptoCurrency currencyInUse = CryptoCurrency.NOLLAR;

    public static final String TAG = SendCoinsPresenter.class.getSimpleName();

    private String targetAddress;

    private String currentInput = "";

    private final NeuroWallet nosWallet = NOSApplication.getNosWallet();
    private final Realm realm;
    private CryptoCurrencyFormatter currencyFormatter = new CryptoCurrencyFormatter().useCurrency(currencyInUse);

    private String recentTypedCoins = "";

    @Inject
    public SendCoinsPresenter(Realm realm) {
        this.realm = realm;
    }

    public void attemptSendCoins(String _coinsAmount) {
        Log.w(TAG, "attemptSendCoins: " + _coinsAmount);

        String rawAmount = currencyFormatter.uiToRaw(_coinsAmount);
        Log.w(TAG, "raw amount is : " + rawAmount);

        if (!targetAddressValid()) {
            String message = view.getString(R.string.please_specify_destination_address);
            view.showError(message);
            return;
        }

        System.out.println("execute send()");

        if (canTransferRawAmount(rawAmount)) {
            view.showLoading();

            String sendAmount = (rawAmount);

            if (websocketMachineRef != null) {
                websocketMachineRef.transferCoins(sendAmount, targetAddress, currencyInUse);
                view.showLoading();
            }
        } else {
            view.showError(R.string.send_error_alert_title, R.string.cannot_transfer);
        }
    }

    public void setTargetAddress(String address) {
        targetAddress = address;
    }

    public void onDeleteTyped() {
        if (!currentInput.isEmpty()) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            view.onCurrentInputReceived(currentInput);
        } else {
            view.onCurrentInputReceived(currentInput = "0");
        }
    }

    public void onDotTyped() {
        if (!currentInput.isEmpty()) {
            if (!alreadyHaveDot(currentInput)) {
                currentInput = currentInput + ".";
                view.onCurrentInputReceived(currentInput);
            }
        }
    }

    private boolean alreadyHaveDot(String currentInput) {
        char[] array = currentInput.toCharArray();
        for (char ch : array) {
            if (ch == '.') return true;
        }
        return false;
    }

    public void onNumberTyped(String input) {
        if (currentInput.equalsIgnoreCase("0")) {
            currentInput = input;
        } else {
            currentInput = currentInput + input;
        }
        view.onCurrentInputReceived(currentInput);
    }

    public void updateAmountFromCode(CharSequence totalValue) {
        currentInput = currencyFormatter.rawtoUi(totalValue.toString());
        view.onCurrentInputReceived(currentInput);
    }

    public void updateAmount(CharSequence value) {
        if (value.equals(view.getString(R.string.send_keyboard_delete))) {
            //delete performed
            onDeleteTyped();
        } else if (value.equals(view.getString(R.string.send_keyboard_decimal))) {
            // decimal point
            onDotTyped();
        } else {
            // digits
            onNumberTyped(value.toString());
        }
    }

    public Credentials provideCredentials() {
        return realm.where(Credentials.class).findFirst();
    }

    public boolean canTransferRawAmount(String raw) {
        String currentTypedCoins = raw;
        this.recentTypedCoins = currentTypedCoins;
        if (currentTypedCoins == null || currentTypedCoins.isEmpty() || new BigDecimal(currentTypedCoins).equals(BigDecimal.ZERO))
            return false;
        if (websocketMachineRef != null) {
            return nosWallet.canTransferNeuros(currentTypedCoins, websocketMachineRef.getRecentAccountBalanceOf(currencyInUse));
        }
        return false;
    }

    public boolean canTransferNeuros(String currentTypedCoinsUi) {
        String raw = currencyFormatter.uiToRaw(currentTypedCoinsUi);
        return canTransferRawAmount(raw);
    }

    private boolean targetAddressValid() {
        Address destination = new Address(targetAddress, currencyInUse);
        return destination.isValidAddress();
    }

    public String getTotalNeurosAmount() {
        String balance = null;
        if (websocketMachineRef != null) {
            balance = websocketMachineRef.getRecentAccountBalanceOf(currencyInUse);
            NOSApplication.getNosWallet().setNeuros(balance);
        }
        Log.d(TAG, "getTotalNeurosAmount: " + balance);
        return balance;
    }

    private WebsocketMachine websocketMachineRef;

    public void observeWebsocketMachine(WebsocketMachine machine) {
        websocketMachineRef = machine;
        serialDisposable.set(machine.observeUiTriggers(currencyInUse)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.isProcessedBlock()) {
                        view.hideLoading();
                        if (response.error != null) {
                            view.showError(response.error);
                        } else {
                            view.showAmountSent(currencyFormatter.rawtoUi(recentTypedCoins), currencyInUse, targetAddress);
                        }
                    } else if (response.socketClosed()) {
                        view.hideLoading();
                    }
                }, this::onErrorSendCoins)
        );
    }

    private void onErrorSendCoins(Throwable throwable) {
        Log.e(TAG, "onErrorSendCoins: ", throwable);
        view.hideLoading();
        throwable.printStackTrace();
        String errorMessage = view.getString(R.string.failed_to_send_coins);
        view.showError(errorMessage);
    }

    public void cancelWebsocketObservation() {
        Disposable d = serialDisposable.get();
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
    }

    private SerialDisposable serialDisposable = new SerialDisposable();

    public void changeCurrency(String text) {
        currencyInUse = determineNewCurrency(text);
        recentTypedCoins = "";
        currencyFormatter = currencyFormatter.useCurrency(currencyInUse);
        view.onCurrentInputReceived("");
        view.onNewCurrencyReceived(currencyInUse);
    }

    private CryptoCurrency determineNewCurrency(String text) {
        CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(text);
        if (cryptoCurrency == CryptoCurrency.NOLLAR) {
            return CryptoCurrency.NOS;
        } else {
            return CryptoCurrency.NOLLAR;
        }
    }
}
