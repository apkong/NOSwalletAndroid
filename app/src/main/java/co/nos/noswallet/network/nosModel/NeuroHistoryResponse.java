package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class NeuroHistoryResponse implements Serializable {

    @SerializedName("account")
    public String account;

    @SerializedName("history")
    public ArrayList<AccountHistory> history = new ArrayList<>();

    public NeuroHistoryResponse() {
    }

}
