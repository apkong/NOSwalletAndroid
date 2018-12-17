package co.nos.noswallet.db;

import javax.annotation.Nullable;

import co.nos.noswallet.persistance.currency.CryptoCurrency;

public interface CredentialsProvider {

    @Nullable
    String providePublicKey();

    @Nullable
    String provideAccountNumber(CryptoCurrency cryptoCurrency);

    @Nullable
    String providePrivateKey();

    @Nullable
    String provideAccountNumber();

}
