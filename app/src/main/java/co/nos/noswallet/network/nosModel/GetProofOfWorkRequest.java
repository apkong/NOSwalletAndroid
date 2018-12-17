package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class GetProofOfWorkRequest implements Serializable {

    @SerializedName("hash")
    public String frontier;

    @SerializedName("action")
    public String action = "get_pow";

    @SerializedName("currency")
    public String currency;

    public GetProofOfWorkRequest(String frontier, CryptoCurrency cryptoCurrency) {
        this.frontier = frontier;
        this.currency = cryptoCurrency.getCurrencyCode();
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
