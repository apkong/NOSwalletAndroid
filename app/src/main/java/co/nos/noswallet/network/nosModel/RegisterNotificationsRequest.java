package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.S;

public class RegisterNotificationsRequest implements Serializable {

    @SerializedName("action")
    public String action = "register_notifications";

    @SerializedName("currency")
    public String currency;

    @SerializedName("account")
    public String account;

    @SerializedName("registration_id")
    public String registration_id;

    @SerializedName("resign")
    public boolean resign;

    public RegisterNotificationsRequest(String account,
                                        String registration_id,
                                        CryptoCurrency cryptoCurrency) {
        this.account = account;
        this.registration_id = registration_id;
        this.currency = cryptoCurrency.getCurrencyCode();
        register();
    }

    public RegisterNotificationsRequest register() {
        this.resign = false;
        return this;
    }

    public RegisterNotificationsRequest unregister() {
        this.resign = true;
        return this;
    }

    @Override
    public String toString() {
        return S.GSON.toJson(this);
    }
}
