package co.nos.noswallet.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import co.nos.noswallet.model.AvailableCurrency;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.ui.home.v2.CurrencyPresenter;

/**
 * Shared Preferences utility module
 */
public class SharedPreferencesUtil {
    private static final String LOCAL_CURRENCY = "local_currency";
    private static final String APP_INSTALL_UUID = "app_install_uuid";
    private static final String CONFIRMED_SEED_BACKEDUP = "confirmed_seed_backedup";
    private static final String FROM_NEW_WALLET = "from_new_wallet";

    private final SharedPreferences preferences;

    public SharedPreferencesUtil(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean has(String key) {
        return preferences.contains(key);
    }

    public String get(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public void clear(String key) {
        set(key, null);
    }


    public SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

    public void set(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();

        if (value != null) {
            editor.putString(key, value);
        } else {
            editor.remove(key);
        }

        editor.commit();
    }

    public void set(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);

        editor.commit();
    }

    public boolean hasLocalCurrency() {
        return has(LOCAL_CURRENCY);
    }

    public AvailableCurrency getLocalCurrency() {
        return AvailableCurrency.valueOf(get(LOCAL_CURRENCY, AvailableCurrency.USD.toString()));
    }

    public void setLocalCurrency(AvailableCurrency localCurrency) {
        set(LOCAL_CURRENCY, localCurrency.toString());
    }

    public void clearLocalCurrency() {
        set(LOCAL_CURRENCY, null);
    }

    public boolean hasAppInstallUuid() {
        return has(APP_INSTALL_UUID);
    }

    public String getAppInstallUuid() {
        return get(APP_INSTALL_UUID, UUID.randomUUID().toString());
    }

    public void setAppInstallUuid(String appInstallUuid) {
        set(APP_INSTALL_UUID, appInstallUuid);
    }

    public void clearAppInstallUuid() {
        set(APP_INSTALL_UUID, null);
    }

    public boolean hasFromNewWallet() {
        return has(FROM_NEW_WALLET);
    }

    public Boolean getFromNewWallet() {
        return get(FROM_NEW_WALLET, false);
    }

    public void setFromNewWallet(Boolean fromNewWallet) {
        set(FROM_NEW_WALLET, fromNewWallet);
    }

    public void clearFromNewWallet() {
        set(FROM_NEW_WALLET, false);
    }

    public boolean hasConfirmedSeedBackedUp() {
        return has(CONFIRMED_SEED_BACKEDUP);
    }

    public Boolean getConfirmedSeedBackedUp() {
        return get(CONFIRMED_SEED_BACKEDUP, false);
    }

    public void setConfirmedSeedBackedUp(Boolean confirmedSeedBackedUp) {
        set(CONFIRMED_SEED_BACKEDUP, confirmedSeedBackedUp);
    }

    public void clearConfirmedSeedBackedUp() {
        set(CONFIRMED_SEED_BACKEDUP, false);
    }

    public void clearAll() {
        clearLocalCurrency();
        clearConfirmedSeedBackedUp();

        SharedPreferences.Editor editor = getEditor();
        for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
            editor.putString(CurrencyPresenter.ACCOUNT_INFO + cryptoCurrency.name(), null);
            editor.putString(CurrencyPresenter.ACCOUNT_HISTORY + cryptoCurrency.name(), null);
        }
        editor.commit();
    }


}
