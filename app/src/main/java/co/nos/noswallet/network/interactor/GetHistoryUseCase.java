package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.realm.Realm;

@Deprecated
public class GetHistoryUseCase {

    private final NeuroClient neuroClient;
    private String accountNumber;


    public volatile NeuroHistoryResponse cachedResponse;

    @Inject
    GetHistoryUseCase(NeuroClient neuroClient, Realm realm) {
        this.neuroClient = neuroClient;
        this.accountNumber = provideAccountNumber(realm);
    }

    private String provideAccountNumber(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            return credentials.getAddressString();
        } else {
            return null;
        }
    }

    public Observable<NeuroHistoryResponse> execute() {
        return Observable.error(new OnErrorNotImplementedException(new Throwable("deprecated API")));
//        return executeFresh();
    }

    private Observable<NeuroHistoryResponse> executeFresh() {
        return neuroClient.getAccountHistory(new NeuroHistoryRequest(accountNumber, "100"))
                .map(s -> {
                    cachedResponse = s;
                    return s;
                });
    }
}
