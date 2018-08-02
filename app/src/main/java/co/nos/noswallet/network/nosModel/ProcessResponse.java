package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

public class ProcessResponse {

    @SerializedName("error")
    public String error;

    @SerializedName("hash")
    public String hash;

    public boolean isSuccessfull() {
        return hash != null;
    }

    @Override
    public String toString() {
        return "ProcessResponse{" +
                "error='" + error + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
