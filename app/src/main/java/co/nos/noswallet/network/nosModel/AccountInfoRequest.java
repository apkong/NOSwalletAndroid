package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class AccountInfoRequest extends BaseWebsocketRequest {

    private transient String currencyCode;

    @SerializedName("account")
    public String account;

    public AccountInfoRequest(String account, CryptoCurrency cryptoCurrency) {
        this.account = account;
        currencyCode = cryptoCurrency.getCurrencyCode();
    }

    public AccountInfoRequest(String account) {
        this(account, CryptoCurrency.NOLLAR);
    }

    @Override
    public String getActionName() {
        return "get_account_information";
    }

    @Override
    public String getCurrencyCode() {
        return currencyCode;
    }

}
