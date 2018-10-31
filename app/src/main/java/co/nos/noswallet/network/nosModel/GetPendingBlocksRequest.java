package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;
import io.reactivex.annotations.Experimental;

public class GetPendingBlocksRequest implements Serializable {

    @SerializedName("account")
    public String account;

    @SerializedName("count")
    public String count;


    @SerializedName("action")
    public String action = "get_pending_blocks";

    @SerializedName("currency")
    public String currency;

    @Deprecated
    public GetPendingBlocksRequest(String account, String count) {
        this(account, count, CryptoCurrency.NOLLAR);
    }

    public GetPendingBlocksRequest(String account, String count, CryptoCurrency currency) {
        this.account = account;
        this.count = count;
        this.currency = currency.getCurrencyCode();
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }

}
