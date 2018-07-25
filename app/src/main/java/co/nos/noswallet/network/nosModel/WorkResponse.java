package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WorkResponse implements Serializable {
    @SerializedName("work")
    public String work;
}
