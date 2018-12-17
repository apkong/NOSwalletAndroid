package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class GetAccountHistoryRequest implements Serializable {
    @SerializedName("action")
    public String action ="get_account_history";

    @SerializedName("currency")
    public String currency;

    @SerializedName("account")
    public String account;

    @SerializedName("block")
    public String block;

    public GetAccountHistoryRequest(String account, String block, CryptoCurrency cryptoCurrency) {
        this.account = account;
        this.block = block;
        this.currency = cryptoCurrency.getCurrencyCode();
    }

    public GetAccountHistoryRequest(String account, String block) {
        this.account = account;
        this.block = block;
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
