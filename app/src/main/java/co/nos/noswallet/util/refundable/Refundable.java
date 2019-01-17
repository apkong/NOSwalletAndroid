package co.nos.noswallet.util.refundable;

public interface Refundable {

    void attemptRefund(RefundableBundle bundle);
}
