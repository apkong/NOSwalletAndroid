package co.nos.noswallet.kyc;

import com.google.gson.annotations.SerializedName;

import javax.inject.Inject;
import javax.inject.Singleton;

public class KycUserDataRepository {

    @SerializedName("dialCode")
    public String dialCode;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("pinCode")
    public String pinCode;

    @SerializedName("smsCode")
    public String smsCode;

    public String firstName;

    public String lastName;

    public String birthDate;

    public void clear() {

    }


    @Inject
    KycUserDataRepository() {

    }
}
