package co.nos.noswallet.network.model.request;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.network.model.Actions;
import co.nos.noswallet.network.model.BaseRequest;
import co.nos.noswallet.network.model.Actions;

/**
 * Retrieve hash history
 */

public class GetBlockRequest extends BaseRequest {
    @SerializedName("action")
    private String action;

    @SerializedName("hash")
    private String hash;


    public GetBlockRequest() {
        this.action = Actions.GET_BLOCK.toString();
    }

    public GetBlockRequest(String hash) {
        this.action = Actions.GET_BLOCK.toString();
        this.hash = hash;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
