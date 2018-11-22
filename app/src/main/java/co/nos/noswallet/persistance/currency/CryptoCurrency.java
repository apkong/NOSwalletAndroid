package co.nos.noswallet.persistance.currency;

import android.util.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public enum CryptoCurrency implements Serializable {


    NOLLAR("usd_", "usd", 2, 0),

    NOS("nos_", "nos", 10, 1),

    BANANO("ban_", "ban", 29, 2),

    NANO("nano_", "nano", 30, 3);

    private static final String TAG = CryptoCurrency.class.getSimpleName();

    private final String prefix;
    private final String currencyCode;
    private final BigDecimal divider;
    private final int dividerLength;

    private final int position;

    CryptoCurrency(String prefix, String currencyCode,
                   int dividerLength, int position) {
        this.prefix = prefix;
        this.currencyCode = currencyCode;
        this.dividerLength = dividerLength;
        this.divider = new BigDecimal(10).pow(this.dividerLength);
        this.position = position;
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

    public String uiToRaw(final String uiValue) {
        if (uiValue.isEmpty()) {
            return "0";
        }
        BigInteger integer = null;

        try {
            integer = new BigDecimal(uiValue).multiply(divider).toBigIntegerExact();
            Log.e(TAG, "uiToRaw: ui: " + uiValue + " => raw: ");
        } catch (ArithmeticException x) {
            Log.e(TAG, "uiToRaw: ui: " + uiValue + " => raw: " + x);
        }
        if (integer != null) {
            return integer.toString();
        }
        return "0";
    }

    public String rawToUi(final String raw) {
        String ui = raw;
        if (this == NOLLAR) ui = rawNollarToUi(raw);
        else ui = rawNosToUi(raw);
        Log.e(TAG, "rawToUi: raw: " + raw + " => ui: " + ui);

        return ui;
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
        Log.w(TAG, "rawNosToUi: " + raw);
        if (raw == null || raw.equals("0")) {
            return "0.00";
        } else if (raw.contains(".")) {
            return raw;
        } else {
            final int length = raw.length();
            if (length == 1) {
                return "0.0" + raw;
            } else if (length == 2) {
                return "0." + raw;
            } else if (dividerTooLarge()) {
                if (length <= dividerLength) {
                    return "0." + zeros(dividerLength - length) + raw.substring(0, NOS.dividerLength);
                } else {
                    return new StringBuilder(raw).insert(length - dividerLength, ".").toString().substring(0, NOS.dividerLength);
                }
            } else if (length <= dividerLength) {
                return "0." + zeros(dividerLength - length) + raw;
            } else {
                return new StringBuilder(raw).insert(length - dividerLength, ".").toString();
            }
        }
    }

    private boolean dividerTooLarge() {
        return dividerLength >  NOS.dividerLength;
    }

    public int getPosition() {
        return position;
    }

    private String zeros(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }

    /**
     * @return next neighbour for selected currency.
     */
    public CryptoCurrency serveNeighbour() {
        CryptoCurrency out = values()[0];
        int size = values().length;

        if (this != values()[size - 1]) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i] == this) {
                    out = values()[i + 1];
                    break;
                }
            }
        }
        Log.d(TAG, "serveNeighbour() called on " + name() + ", returning " + out.name());

        return out;
    }
}
