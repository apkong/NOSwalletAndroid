package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.NeuroHistoryRequest;
import co.nos.noswallet.network.nosModel.NeuroHistoryResponse;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GetHistoryUseCase {

    private final NeuroClient neuroClient;

    @Inject
    GetHistoryUseCase(NeuroClient neuroClient) {
        this.neuroClient = neuroClient;
    }

    public Observable<NeuroHistoryResponse> execute(String accountNumber) {

        return neuroClient.getAccountHistory(new NeuroHistoryRequest(accountNumber, "100"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
