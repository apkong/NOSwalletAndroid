package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

public class AccountInfoRequest {

    @SerializedName("action")
    public String action = "account_info";

    @SerializedName("account")
    public String account;

    public AccountInfoRequest() {
    }

    public AccountInfoRequest(String account) {
        this.account = account;
    }


}
