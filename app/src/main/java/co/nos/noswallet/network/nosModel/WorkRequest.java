package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WorkRequest implements Serializable {

    @SerializedName("action")
    public String action = "work_generate";

    @SerializedName("hash")
    public String hash;

    public WorkRequest(String hash) {
        this.hash = hash;
    }
}
