package co.nos.noswallet.kyc;

import com.google.gson.annotations.SerializedName;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.nos.noswallet.kyc.homeAddress.Country;

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

    public String postalCode;

    public String region;

    public String street;

    public String addressPart1;

    public String addressPart2;

    public Country country;
    public String email;

    public void clear() {

    }


    @Inject
    KycUserDataRepository() {

    }
}
