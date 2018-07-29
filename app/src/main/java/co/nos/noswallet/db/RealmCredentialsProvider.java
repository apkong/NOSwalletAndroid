package co.nos.noswallet.db;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import io.realm.Realm;

public class RealmCredentialsProvider implements CredentialsProvider {

    private final Realm realm;

    @Inject
    RealmCredentialsProvider(Realm realm) {
        this.realm = realm;
    }

    private Credentials provideCredentials(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        return credentials;
    }

    @Override
    public String providePublicKey() {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getPublicKey();
        } else {
            return null;
        }
    }

    @Override
    public String providePrivateKey() {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getPrivateKey();
        } else {
            return null;
        }
    }

    @Override
    public String provideAccountNumber() {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getAddressString();
        } else {
            return null;
        }
    }
}
