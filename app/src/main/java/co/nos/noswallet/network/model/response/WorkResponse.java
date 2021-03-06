package co.nos.noswallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.network.model.BaseResponse;

/**
 * Pushed price data - currently sent every minute to all clients
 */

public class WorkResponse extends BaseResponse {
    @SerializedName("work")
    private String work;

    public WorkResponse() {
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }
}
