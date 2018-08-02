package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class AccountInfoResponse {

    @SerializedName("frontier")
    @Nullable
    public String frontier;

    @SerializedName("open_block")
    @Nullable
    public String open_block;

    @SerializedName("representative_block")
    @Nullable
    public String representative_block;

    @SerializedName("balance")
    @Nullable
    public String balance;

    @SerializedName("modified_timestamp")
    @Nullable
    public String modified_timestamp;

    @SerializedName("block_count")
    @Nullable
    public String block_count;

    @SerializedName("error")
    @Nullable
    public String error;

    public AccountInfoResponse() {
    }

    public boolean isFreshAccount() {
        return error != null || balance == null;
    }
}
