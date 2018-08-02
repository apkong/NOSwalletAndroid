package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class GetHistoryUseCase {

    private final NeuroClient neuroClient;
    private String accountNumber;


    private volatile NeuroHistoryResponse cachedResponse;

    @Inject
    GetHistoryUseCase(NeuroClient neuroClient, Realm realm) {
        this.neuroClient = neuroClient;
        this.accountNumber = provideAccountNumber(realm);
    }

    String provideAccountNumber(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            return credentials.getAddressString();
        } else {
            return null;
        }
    }

    public Observable<NeuroHistoryResponse> execute() {
        if (accountNumber == null) {
            return Observable.error(new IllegalStateException("missing seed!!!"));
        }

        if (cachedResponse != null) {
            return Observable.fromCallable(() -> cachedResponse).concatWith(executeFresh());
        } else {
            return executeFresh();
        }
    }

    private Observable<NeuroHistoryResponse> executeFresh() {
        return neuroClient.getAccountHistory(new NeuroHistoryRequest(accountNumber, "100"))
                .map(s -> {
                    cachedResponse = s;
                    return s;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
