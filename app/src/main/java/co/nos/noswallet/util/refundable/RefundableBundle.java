package co.nos.noswallet.util.refundable;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class RefundableBundle implements Serializable {
    public CryptoCurrency cryptoCurrency;
    public String rawAmount;
    public String targetAddress;

    public RefundableBundle(CryptoCurrency cryptoCurrency,
                            String rawAmount,
                            String targetAddress) {
        this.cryptoCurrency = cryptoCurrency;
        this.rawAmount = rawAmount;
        this.targetAddress = targetAddress;
    }

    @Override
    public String toString() {
        return "RefundableBundle{" +
                "cryptoCurrency=" + cryptoCurrency +
                ", rawAmount='" + rawAmount + '\'' +
                ", targetAddress='" + targetAddress + '\'' +
                '}';
    }
}
