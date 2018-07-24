package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    @SerializedName("private")
    public String _private;

    @SerializedName("public")
    public String _public;

    @SerializedName("account")
    public String account;

    public LoginResponse() {
    }

    public boolean isValid() {
        return _private != null && _public != null && account != null;
    }
}
