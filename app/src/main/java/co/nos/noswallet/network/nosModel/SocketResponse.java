package co.nos.noswallet.network.nosModel;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class SocketResponse {

    @SerializedName("action")
    public String action;

    @SerializedName("currency")
    public String currency;

    @SerializedName("error")
    public String error;

    @SerializedName("response")
    public JsonElement response;

    public SocketResponse() {
    }

    @Override
    public String toString() {
        return "SocketResponse{" +
                "action='" + action + '\'' +
                ", currency='" + currency + '\'' +
                ", error='" + error + '\'' +
                ", response=" + String.valueOf(response) +
                '}';
    }

    public boolean isHistoryResponse() {
        return "get_account_history".equals(action);
    }

    public boolean isAccountInformationResponse() {
        return "get_account_information".equals(action);
    }

    public boolean isProcessedBlock() {
        return "publish_block".equals(action);
    }
}