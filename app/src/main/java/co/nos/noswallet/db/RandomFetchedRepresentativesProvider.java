package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.model.PreconfiguredRepresentatives;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.SharedPreferencesUtil;

public class RandomFetchedRepresentativesProvider implements RepresentativesProvider {

    public static final String OWN_REPRESENTATIVE = "OWN_REPRESENTATIVE";

    private final SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    public RandomFetchedRepresentativesProvider(SharedPreferencesUtil sharedPreferencesUtil) {
        this.sharedPreferencesUtil = sharedPreferencesUtil;
    }

    @Override
    public String provideRepresentative(CryptoCurrency cryptoCurrency) {
        if (hasOwnRepresentative(cryptoCurrency)) {
            return getOwnRepresentative(cryptoCurrency);
        }
        return PreconfiguredRepresentatives.getRepresentative(cryptoCurrency);
    }

    @Override
    public boolean hasOwnRepresentative(CryptoCurrency currency) {
        return getOwnRepresentative(currency) != null;
    }

    @Override
    public void setOwnRepresentative(CryptoCurrency currency, String representative) {
        String key = buildKeyWith(currency);

        sharedPreferencesUtil.getEditor()
                .putString(key, representative)
                .commit();
    }

    private String buildKeyWith(CryptoCurrency currency) {
        return OWN_REPRESENTATIVE + "_" + currency.name();
    }

    private String getOwnRepresentative(CryptoCurrency currency) {
        String key = buildKeyWith(currency);
        return sharedPreferencesUtil.get(key, null);
    }
}
