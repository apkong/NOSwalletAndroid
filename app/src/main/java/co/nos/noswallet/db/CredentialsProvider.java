package co.nos.noswallet.db;

import javax.annotation.Nullable;

public interface CredentialsProvider {

    @Nullable
    String providePublicKey();

    @Nullable
    String providePrivateKey();

    @Nullable
    String provideAccountNumber();

}
