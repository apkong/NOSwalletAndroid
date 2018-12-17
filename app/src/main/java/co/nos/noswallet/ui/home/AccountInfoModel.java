package co.nos.noswallet.ui.home;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AccountInfoModel implements Serializable {

    @SerializedName("frontier_block_hash")
    public String frontier;

    @SerializedName("balance")
    public String balance;

    @SerializedName("block_count")
    public String block_count;

    @SerializedName("representative")
    public String representative;

    public AccountInfoModel() {
    }


}
