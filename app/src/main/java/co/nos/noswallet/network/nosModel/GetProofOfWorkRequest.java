package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class GetProofOfWorkRequest extends BaseWebsocketRequest {

    @SerializedName("hash")
    public String frontier;

    public GetProofOfWorkRequest(String frontier) {
        this.frontier = frontier;
    }

    @Override
    public String getActionName() {
        return "get_pow";
    }

    @Override
    public String getCurrencyCode() {
        return CryptoCurrency.NOLLAR.getCurrencyCode();
    }
}
