package co.nos.noswallet.persistance.currency;

public enum CryptoCurrency {

    NEURO("eur_", "eur"),

    NOLLAR("usd_", "usd"),
    UNKNOWN("_", "N/A");

    private final String prefix;
    private final String currencyCode;

    CryptoCurrency(String prefix, String currencyCode) {
        this.prefix = prefix;
        this.currencyCode = currencyCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefixWithNoFloor() {
        return prefix.replace("_", "");
    }
}
