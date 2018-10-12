package co.nos.noswallet.network.websockets.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetAccountInformationResponse implements Serializable {

    @SerializedName("action")
    public String action;

    @SerializedName("currency")
    public String currency;

    @SerializedName("error")
    public String error;

    @SerializedName("response")
    public JsonElement response;

}
