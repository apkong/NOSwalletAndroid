package co.nos.noswallet.persistance.currency;

import android.util.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public enum CryptoCurrency implements Serializable {


    NOLLAR("usd_", "usd", 2),

    NOS("nos_", "nos", 10);

    private final String prefix;
    private final String currencyCode;
    private final BigDecimal divider;
    private final DecimalFormat decimalFormat;


    CryptoCurrency(String prefix, String currencyCode,
                   int dividerLength,
                   String format) {
        this.prefix = prefix;
        this.currencyCode = currencyCode;
        this.divider = new BigDecimal(10).pow(dividerLength);
        this.decimalFormat = new DecimalFormat(format);
    }

    CryptoCurrency(String prefix, String currencyCode,
                   int dividerLength) {
        this(prefix, currencyCode, dividerLength, "#0.##");
    }

    public static String formatWith(String currency, String balance) {
        CryptoCurrency recognized = recognize(currency);
        return balance + " " + recognized.name();
    }

    public static CryptoCurrency recognize(String text) {
        for (CryptoCurrency c : values()) {
            if (c.name().equalsIgnoreCase(text)) return c;
            if (c.currencyCode.equalsIgnoreCase(text)) return c;
        }
        return NOLLAR;
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

    public String uiToRaw(String uiValue) {
        if (uiValue.isEmpty()) {
            return "0";
        }
        return new BigDecimal(uiValue).multiply(divider).toString();
    }

    public String rawToUi(String raw) {
        if (this == NOLLAR) return rawNollarToUi(raw);
        if (this == NOS) return rawNosToUi(raw);
        return raw;
    }

    private String rawNollarToUi(String raw) {
        if (raw == null || raw.equals("0")) {
            return "0.00";
        } else if (raw.contains(".")) {
            return raw;
        } else {
            final int length = raw.length();
            switch (length) {
                case 1:
                    return "0.0" + raw;
                case 2:
                    return "0." + raw;
                case 3:
                default:
                    return new StringBuilder(raw).insert(length - 2, ".").toString();
            }
        }
    }

    private String rawNosToUi(String raw) {
        Log.w(name(), "rawNosToUi: " + raw);
        if (raw == null || raw.equals("0")) {
            return "0.00";
        } else if (raw.contains(".")) {
            return raw;
        } else {
            final int length = raw.length();

            if (length <= 10) {
                return "0." + zeros(10 - length) + raw;
            }

//            if (length == 7) {
//                return "~0.000" + raw;
//            }
//            if (length == 8) {
//                return "~0.00" + raw;
//            }
//            if (length == 9) {
//                return "0.0" + raw;
//            }
//            if (length == 10) {
//                return "0." + raw;
//
//            }
            return new StringBuilder(raw).insert(length - 10, ".").toString();
        }
    }

    private String zeros(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }
}
