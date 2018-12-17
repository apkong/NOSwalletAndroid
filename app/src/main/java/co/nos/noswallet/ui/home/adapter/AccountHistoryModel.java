package co.nos.noswallet.ui.home.adapter;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AccountHistoryModel implements Serializable {

    @SerializedName("modified_timestamp")
    public String modified_timestamp;

    @SerializedName("balance")
    public String balance;
}
