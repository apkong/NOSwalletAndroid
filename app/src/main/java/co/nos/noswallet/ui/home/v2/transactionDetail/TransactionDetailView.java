package co.nos.noswallet.ui.home.v2.transactionDetail;

import co.nos.noswallet.util.refundable.RefundableBundle;

public interface TransactionDetailView {

    void onBlockHashLinkReceived(String url);

    String getString(int resId, Object... args);

    void onReceiveTransferParams(int drawableRes,
                                 String receiveSendMessage,
                                 boolean shouldShowRefundButton,
                                 String hash,
                                 String address);

    void navigateToRefundScreen(RefundableBundle bundle);
}
