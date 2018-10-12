package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import io.reactivex.annotations.Experimental;

public class GetPendingBlocksRequest extends BaseWebsocketRequest {

    private transient CryptoCurrency cryptoCurrency;

    @SerializedName("account")
    public String account;

    @SerializedName("count")
    public String count;

    @Experimental
    public GetPendingBlocksRequest(String account, String count) {
        this(account, count, CryptoCurrency.NOLLAR);
    }

    public GetPendingBlocksRequest(String account, String count, CryptoCurrency currency) {
        this.account = account;
        this.count = count;
        this.cryptoCurrency = currency;
    }

    @Override
    public String getActionName() {
        return "get_pending_blocks";
    }

    @Override
    public String getCurrencyCode() {
        return CryptoCurrency.NOLLAR.getCurrencyCode();
    }

}
