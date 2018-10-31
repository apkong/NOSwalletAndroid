package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.model.PreconfiguredRepresentatives;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class RandomFetchedRepresentativesProvider implements RepresentativesProvider {

    @Inject
    public RandomFetchedRepresentativesProvider() {
    }

    @Override
    public String provideRepresentative() {
        return PreconfiguredRepresentatives.getRepresentative();
    }

    @Override
    public String provideRepresentative(CryptoCurrency cryptoCurrency) {
        return PreconfiguredRepresentatives.getRepresentative(cryptoCurrency);
    }
}
