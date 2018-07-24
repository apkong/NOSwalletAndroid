package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetPendingBlocksRequest implements Serializable {

    @SerializedName("action")
    public String action = "pending";

    @SerializedName("account")
    public String account;

    @SerializedName("count")
    public String count;

    public GetPendingBlocksRequest(String account, String count) {
        this.account = account;
        this.count = count;
    }

    public GetPendingBlocksRequest() {
    }
}
