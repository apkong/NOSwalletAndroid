package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class GetAccountHistoryRequest extends BaseWebsocketRequest {

    @SerializedName("account")
    public String account;

    @SerializedName("block")
    public String block;

    public GetAccountHistoryRequest(String account, String block) {
        this.account = account;
        this.block = block;
    }

    @Override
    public String getActionName() {
        return "get_account_history";
    }

    @Override
    public String getCurrencyCode() {
        return CryptoCurrency.NOLLAR.getCurrencyCode();
    }
}
