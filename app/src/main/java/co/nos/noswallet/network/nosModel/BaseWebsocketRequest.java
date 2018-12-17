package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.util.S;

public abstract class BaseWebsocketRequest implements Serializable {

    @SerializedName("action")
    public String action = getActionName();

    @SerializedName("currency")
    public String currency = getCurrencyCode();

    public abstract String getActionName();

    public abstract String getCurrencyCode();

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
