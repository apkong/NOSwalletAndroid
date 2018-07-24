package co.nos.noswallet.network.interactor;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.LoginRequest;
import co.nos.noswallet.network.nosModel.LoginResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.realm.Realm;

public class LoginUseCase {

    private final NeuroClient neuroClient;
    private Handler handler;
    private Realm _realm;

    @Inject
    LoginUseCase(NeuroClient client,
                 Realm realm) {
        neuroClient = client;
        handler = new Handler(Looper.getMainLooper());
        _realm = realm;
    }

    public Observable<LoginResponse> execute(String seed) {
        return neuroClient.login(new LoginRequest(seed, "0"))
                .flatMap((Function<LoginResponse, ObservableSource<LoginResponse>>) loginResponse -> {
                    if (loginResponse.isValid()) {
                        handler.post(() -> saveCredentials(loginResponse));
                    } else {
                        return Observable.error(new IllegalStateException("invalid credentials"));
                    }
                    return Observable.just(loginResponse);
                });

    }

    private void saveCredentials(LoginResponse loginResponse) {
        _realm.executeTransaction(realm -> {
            Credentials credentials = realm.where(Credentials.class).findFirst();
            if (credentials != null) {
                credentials.setPrivateKey(loginResponse._private);
                realm.copyToRealmOrUpdate(credentials);
            }
        });
    }
}
