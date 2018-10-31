package co.nos.noswallet.db;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public interface RepresentativesProvider {

    String provideRepresentative();

    String provideRepresentative(CryptoCurrency cryptoCurrency);

    default void setRepresentative(String representative) {
    }
}
