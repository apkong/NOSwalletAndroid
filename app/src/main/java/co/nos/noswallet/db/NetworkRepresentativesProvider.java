package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.SharedPreferencesUtil;

public class NetworkRepresentativesProvider implements RepresentativesProvider {
    private static final String KEY = "Representative";

    private volatile String representative;
    private SharedPreferencesUtil util;

    @Inject
    public NetworkRepresentativesProvider(SharedPreferencesUtil util) {
        this.util = util;
    }

    @Override
    public String provideRepresentative() {
        if (representative == null) {
            this.representative = util.get(KEY, null);
        }
        System.err.println("returning nullable representative : " + representative);
        return representative;
    }

    @Override
    public String provideRepresentative(CryptoCurrency cryptoCurrency) {
        return null;
    }

    @Override
    public void setRepresentative(String representative) {
        this.representative = representative;
        util.set(KEY, representative);
    }
}
