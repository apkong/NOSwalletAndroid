package co.nos.noswallet.ui.send;

import co.nos.noswallet.base.BaseView;

public interface SendCoinsView extends BaseView {

    void onCurrentInputReceived(String currentInput);

    String getString(int key);

    void showError(String message);

    void showError(int title, int message);

    void showAmountSent(String sendAmount, String targetAddress);
}
