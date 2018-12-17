package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class AccountInfoRequest implements Serializable {

    @SerializedName("action")
    public String action = "get_account_information";

    @SerializedName("currency")
    public String currency;

    @SerializedName("account")
    public String account;

    public AccountInfoRequest(String account, CryptoCurrency cryptoCurrency) {
        this.account = account;
        currency = cryptoCurrency.getCurrencyCode();
    }

    @Deprecated
    public AccountInfoRequest(String account) {
        this(account, CryptoCurrency.NOLLAR);
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }

}
