package co.nos.noswallet.network.websockets.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetPendingBlocksResponse implements Serializable {

    @SerializedName("currency")
    public String currency;

    @SerializedName("response")
    public JsonElement response;


}
