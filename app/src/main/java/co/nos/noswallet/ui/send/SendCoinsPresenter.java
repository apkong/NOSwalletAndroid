package co.nos.noswallet.ui.send;

import java.math.BigDecimal;

import javax.inject.Inject;

import co.nos.noswallet.R;
import co.nos.noswallet.base.BasePresenter;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.websockets.WebsocketMachine;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.NosLogger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.SerialDisposable;
import io.realm.Realm;

public class SendCoinsPresenter extends BasePresenter<SendCoinsView> {

    CryptoCurrency currencyInUse = CryptoCurrency.NOLLAR;

    public static final String TAG = SendCoinsPresenter.class.getSimpleName();

    private String targetAddress;

    private String currentInput = "";

    private boolean loadingIsVisible = false;

    private final Realm realm;
    private CryptoCurrencyFormatter currencyFormatter = new CryptoCurrencyFormatter().useCurrency(currencyInUse);
    private SerialDisposable serialDisposable = new SerialDisposable();

    private String recentTypedCoins = "";

    private WebsocketMachine websocketMachineRef;

    @Inject
    public SendCoinsPresenter(Realm realm) {
        this.realm = realm;
    }


    public void resetCurrentInput() {
        currentInput = "";
        recentTypedCoins = "";
    }

    public void reset() {
        targetAddress = null;
        loadingIsVisible = false;
        resetCurrentInput();
    }

    @Override
    public void attachView(SendCoinsView view) {
        super.attachView(view);
        reset();
    }

    public void attemptSendCoins(String uiCoinsAmount) {
        NosLogger.w(TAG, "attemptSendCoins: " + uiCoinsAmount);

        String rawAmount = currencyFormatter.uiToRaw(uiCoinsAmount);
        NosLogger.w(TAG, "raw amount is : " + rawAmount);

        if (!targetAddressValid()) {
            String message = view.getString(R.string.please_specify_destination_address);
            view.hideLoading();
            //view.showError(message);
            view.showSendAttemptError(R.string.please_specify_destination_address);

            return;
        }

        System.out.println("execute send()");

        if (canTransferRawAmount(rawAmount)) {

            showLoading();

            String sendAmount = rawAmount;

            if (websocketMachineRef != null) {
                websocketMachineRef.transferCoins(sendAmount, targetAddress, currencyInUse);
                showLoading();
            }
        } else {
            //view.showError(R.string.send_error_alert_title, R.string.cannot_transfer);
            view.showSendAttemptError(R.string.cannot_transfer);
            hideLoading();
        }
    }

    private void hideLoading() {
        view.hideLoading();
        loadingIsVisible = false;
    }

    private void showLoading() {
        view.showLoading();
        loadingIsVisible = true;
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
        currentInput = currencyFormatter.rawtoUi(totalValue == null ? "" : totalValue.toString());
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
        String rawTypedCoins = raw;
        NosLogger.w(TAG, "canTransferRawAmount: " + rawTypedCoins);
        this.recentTypedCoins = rawTypedCoins;
        if (rawTypedCoins == null || rawTypedCoins.isEmpty() || new BigDecimal(rawTypedCoins).equals(BigDecimal.ZERO))
            return false;
        if (websocketMachineRef != null) {
            return transferPossible(rawTypedCoins, websocketMachineRef.getRecentAccountBalanceOf(currencyInUse));
        }
        return false;
    }

    public boolean transferPossible(String raw_amount, String currentBalance) {
        NosLogger.w(TAG, "transferPossible: " + raw_amount + ", " + currentBalance);
        if (raw_amount == null || raw_amount.isEmpty()) return false;
        if (currentBalance == null) return false;

        BigDecimal difference = new BigDecimal(currentBalance).subtract(new BigDecimal(raw_amount));
        if (difference == null) return false;
        return difference.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean canTransferNeuros(String currentTypedCoinsUi) {
        String raw = currencyFormatter.uiToRaw(currentTypedCoinsUi);
        return canTransferRawAmount(raw);
    }

    public boolean targetAddressValid() {
        Address destination = new Address(targetAddress, currencyInUse);
        return destination.isValidAddress();
    }

    public String getTotalCoinsAmount() {
        String balance = "0";
        if (websocketMachineRef != null) {
            balance = websocketMachineRef.getRecentAccountBalanceOf(currencyInUse);
        }
        NosLogger.d(TAG, "getTotalCoinsAmount: " + balance);

        return balance;
    }

    public void observeWebsocketMachine(WebsocketMachine machine) {
        websocketMachineRef = machine;
        serialDisposable.set(machine.observeUiTriggers(currencyInUse)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (loadingIsVisible) {
                        hideLoading();
                        if (response.isProcessedBlock()) {
                            if (response.error != null) {
                                view.showError(response.error);
                            } else {
                                view.showAmountSent(currencyFormatter.rawtoUi(recentTypedCoins), currencyInUse, targetAddress);
                            }
                        } else if (response.socketClosed()) {
                            onConnectionInterrupted();
                        }
                    }
                }, this::onErrorSendCoins)
        );
    }

    private void onErrorSendCoins(Throwable throwable) {
        NosLogger.e(TAG, "onErrorSendCoins: ", throwable);
        hideLoading();
        throwable.printStackTrace();
        String errorMessage = view.getString(R.string.failed_to_send_coins);
        view.showError(errorMessage);
    }

    private void onConnectionInterrupted() {
        hideLoading();
        String errorMessage = view.getString(R.string.connection_interrupted);
        view.showError(errorMessage, false);
    }

    public void cancelWebsocketObservation() {
        Disposable d = serialDisposable.get();
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
    }

    public void changeCurrencyTo(CryptoCurrency currency) {
        currencyInUse = currency;
        updateOtherCurrencyParameters();
    }

    public void switchCurrency(String text) {
        currencyInUse = determineNewCurrency(text);
        updateOtherCurrencyParameters();
    }

    private void updateOtherCurrencyParameters() {
        currencyFormatter.useCurrency(currencyInUse);
        recentTypedCoins = "";
        view.onCurrentInputReceived(recentTypedCoins);
        view.onNewCurrencyReceived(currencyInUse);
    }

    private CryptoCurrency determineNewCurrency(String text) {
        CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(text);
        return cryptoCurrency.serveNeighbour();
    }
}
