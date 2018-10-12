package co.nos.noswallet.persistance;

import com.google.gson.Gson;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.SharedPreferencesUtil;

public class DefaultUserSession implements UserSession {

    public static final String USER_CURRENCY = "USER_CURRENCY";

    private final SharedPreferencesUtil sharedPreferences;

    public DefaultUserSession(SharedPreferencesUtil sharedPreferences,
                              Gson gson) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public CryptoCurrency getPreferredCurrencyPrefix() {
        CryptoCurrency cryptoCurrency = resolveUsedCurrency();
        return cryptoCurrency;
    }

    private CryptoCurrency resolveUsedCurrency() {
        String currency = sharedPreferences.get(USER_CURRENCY, "");
        for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
            if (currency.equalsIgnoreCase(cryptoCurrency.name())) {
                return cryptoCurrency;
            }
        }
        return CryptoCurrency.UNKNOWN;
    }

    @Override
    public void setPreferredCurrencyPrefix(CryptoCurrency currency) {
        sharedPreferences.set(USER_CURRENCY, "");
    }
}
