package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AccountHistory implements Serializable {

    @SerializedName("type")
    public String type;

    @SerializedName("account")
    public String account;

    @SerializedName("amount")
    public String amount;

    @SerializedName("hash")
    public String hash;

    public AccountHistory(String amount, String account) {
        this.amount = amount;
        this.account = account;
    }

    public AccountHistory() {
    }

    public boolean isSend() {
        return "send".equalsIgnoreCase(type);
    }
}
