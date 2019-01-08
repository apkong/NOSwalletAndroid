package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.SharedPreferencesUtil;
@Deprecated
public class NetworkRepresentativesProvider implements RepresentativesProvider {
    private static final String KEY = "Representative";

    private SharedPreferencesUtil util;

    @Inject
    public NetworkRepresentativesProvider(SharedPreferencesUtil util) {
        this.util = util;
    }

    @Override
    public String provideRepresentative(CryptoCurrency cryptoCurrency) {
        return null;
    }

    @Override
    public void setRepresentative(String representative) {
        util.set(KEY, representative);
    }

    @Override
    public boolean hasOwnRepresentative(CryptoCurrency currency) {
        return false;
    }

    @Override
    public void setOwnRepresentative(CryptoCurrency currency,String s) {

    }
}
