package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

public class ProcessRequest {
    @SerializedName("action") public String action = "process";
    @SerializedName("block") public ProcessBlock block ;

    public ProcessRequest(ProcessBlock block) {
        this.block = block;
    }
}
