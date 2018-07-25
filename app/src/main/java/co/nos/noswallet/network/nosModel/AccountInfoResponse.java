package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

public class AccountInfoResponse {

    @SerializedName("frontier")
    public String frontier;

    @SerializedName("open_block")
    public String open_block;

    @SerializedName("representative_block")
    public String representative_block;

    @SerializedName("balance")
    public String balance;

    @SerializedName("modified_timestamp")
    public String modified_timestamp;

    @SerializedName("block_count")
    public String block_count;

    public AccountInfoResponse() {
    }

}
