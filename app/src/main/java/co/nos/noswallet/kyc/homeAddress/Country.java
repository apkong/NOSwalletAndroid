package co.nos.noswallet.kyc.homeAddress;

public class Country {
    public String code;
    public String displayName;

    public Country() {
    }

    public Country(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
