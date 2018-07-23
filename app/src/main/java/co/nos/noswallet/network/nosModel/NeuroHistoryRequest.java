package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NeuroHistoryRequest implements Serializable {

    @SerializedName("action")
    public String action = "account_history";

    @SerializedName("account")
    public String account;

    @SerializedName("count")
    public String count;

    public NeuroHistoryRequest() {
    }

    public NeuroHistoryRequest(String account, String count) {
        this.account = account;
        this.count = count;
    }
}
