package co.nos.noswallet.network.interactor;

import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class GetPendingBlocksUseCase {

    public static final String TAG = "GetPendingBlocksUseCase";

    private final NeuroClient neuroClient;
    private String accountNumber;
    private SerialDisposable pendingTransactionsDisposable = new SerialDisposable();

    @Inject
    GetPendingBlocksUseCase(NeuroClient neuroClient, Realm realm) {
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

    public Observable<GetPendingBlocksResponse> execute() {
        if (accountNumber == null) {
            return Observable.error(new IllegalStateException("missing seed!!!"));
        }
        return neuroClient.getPendingBlocks(new GetPendingBlocksRequest(accountNumber, "100"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void startObservePendingTransactions() {
        if (pendingTransactionsDisposable == null) {
            pendingTransactionsDisposable = new SerialDisposable();
        }
        pendingTransactionsDisposable.set(
                Observable.interval(15, TimeUnit.SECONDS)
                        .switchMap(new Function<Long, ObservableSource<GetPendingBlocksResponse>>() {
                            @Override
                            public ObservableSource<GetPendingBlocksResponse> apply(Long aLong) throws Exception {
                                return execute()
                                        .onErrorReturnItem(new GetPendingBlocksResponse())
                                        .flatMap(getPendingBlocksResponse -> {
                                            if (getPendingBlocksResponse.isEmpty())
                                                return Observable.empty();
                                            return Observable.just(getPendingBlocksResponse);
                                        });
                            }
                        })

                        .subscribe(new Consumer<GetPendingBlocksResponse>() {
                            @Override
                            public void accept(GetPendingBlocksResponse getPendingBlocksResponse) throws Exception {
                                Log.d(TAG, "onNext: GetPendingBlocksResponse: " + readableBlocks(getPendingBlocksResponse));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(TAG, "onError: throwable: " + throwable.getMessage());
                            }
                        })
        );
    }

    public void stopPendingTransactions() {
        //todo:
        if (pendingTransactionsDisposable != null) {
            pendingTransactionsDisposable.dispose();
            pendingTransactionsDisposable = null;
        }
    }

    private String readableBlocks(GetPendingBlocksResponse getPendingBlocksResponse) {
        return Arrays.toString(getPendingBlocksResponse.blocks.toArray());
    }

}
