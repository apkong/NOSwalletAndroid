package co.nos.noswallet.ui.send;

import co.nos.noswallet.base.BaseView;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public interface SendCoinsView extends BaseView {

    void onCurrentInputReceived(String currentInput);

    String getString(int key);

    void showError(String message);

    void showError(String message, boolean exitScreen);

    void showError(int title, int message);

    void showSendAttemptError(int messageRes);

    void showAmountSent(String sendAmount, CryptoCurrency cryptoCurrency, String targetAddress);

    void onNewCurrencyReceived(CryptoCurrency currencyInUse);
}
