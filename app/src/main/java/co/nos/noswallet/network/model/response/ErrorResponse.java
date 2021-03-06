package co.nos.noswallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.network.model.BaseResponse;

/**
 * SocketClosed response from service
 */

public class ErrorResponse extends BaseResponse {
    @SerializedName("error")
    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
