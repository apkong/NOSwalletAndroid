package co.nos.noswallet.db;

import android.util.Log;

import javax.annotation.Nullable;
import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import io.realm.Realm;

public class RealmCredentialsProvider implements CredentialsProvider {

    private final Realm realm;

    @Inject
    public RealmCredentialsProvider(Realm realm) {
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

    @Nullable
    @Override
    public String provideAccountNumber(CryptoCurrency cryptoCurrency) {
        Credentials credentials = provideCredentials(realm);
        String addressString = null;
        if (credentials != null) {
            addressString = credentials.getAddressString(cryptoCurrency);
        }
        Log.i("REALM_XD", "provideAccountNumber: " + cryptoCurrency.name() + " : " + addressString);
        return addressString;
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
