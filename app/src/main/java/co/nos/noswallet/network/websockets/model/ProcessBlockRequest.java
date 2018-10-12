package co.nos.noswallet.network.websockets.model;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class ProcessBlockRequest {

    @SerializedName("currency")
    public String currency = CryptoCurrency.NOLLAR.getCurrencyCode();

    @SerializedName("action")
    public String action = "publish_block";

    @SerializedName("account")
    public String account;

    @SerializedName("previous")
    public String previous;

    @SerializedName("balance")
    public String balance;

    @SerializedName("link")
    public String link;

    @SerializedName("signature")
    public String signature;

    @SerializedName("pow")
    public String pow;

    public ProcessBlockRequest(String account,
                               String previous,
                               String balance,
                               String link,
                               String signature,
                               String pow) {
        this.account = account;
        this.previous = previous;
        this.balance = balance;
        this.link = link;
        this.signature = signature;
        this.pow = pow;
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
