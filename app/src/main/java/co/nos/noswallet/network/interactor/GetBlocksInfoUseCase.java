package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class GetBlocksInfoUseCase {

    private final NeuroClient neuroClient;
    private String accountNumber;

    @Inject
    GetBlocksInfoUseCase(NeuroClient neuroClient, Realm realm) {
        this.neuroClient = neuroClient;
        this.accountNumber = provideAccountNumber(realm);
    }

    public String provideAccountNumber(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            return credentials.getAddressString();
        } else {
            return null;
        }
    }

    public String getPublicKey(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            return credentials.getPublicKey();
        } else {
            return null;
        }
    }

    public Observable<GetPendingBlocksResponse> execute() {
        if (accountNumber == null) {
            return Observable.error(new IllegalStateException("missing seed!!!"));
        }
        return neuroClient.getPendingBlocks(new GetPendingBlocksRequest(accountNumber, "100"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
