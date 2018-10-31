package co.nos.noswallet.network.websockets.model;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class ProcessBlockRequest {

    @SerializedName("currency")
    public String currency;

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

    @SerializedName("representative")
    public String representative;

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
        this.currency = CryptoCurrency.NOLLAR.getCurrencyCode();
    }

    public ProcessBlockRequest(String account,
                               String previous,
                               String balance,
                               String link,
                               String signature,
                               String pow,
                               CryptoCurrency cryptoCurrency) {
        this.account = account;
        this.previous = previous;
        this.balance = balance;
        this.link = link;
        this.signature = signature;
        this.pow = pow;
        this.currency = cryptoCurrency.getCurrencyCode();
    }

    public ProcessBlockRequest withRepresentative(String representative) {
        this.representative = representative;
        return this;
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
