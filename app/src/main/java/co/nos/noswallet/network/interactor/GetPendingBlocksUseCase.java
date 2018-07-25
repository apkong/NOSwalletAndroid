package co.nos.noswallet.network.interactor;

import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.model.Credentials;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.model.request.GetBlocksInfoRequest;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksResponse;
import co.nos.noswallet.network.nosModel.ProcessBlock;
import co.nos.noswallet.network.nosModel.ProcessRequest;
import co.nos.noswallet.network.nosModel.WorkRequest;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class GetPendingBlocksUseCase {

    public static final String TAG = "GetPendingBlocksUseCase";

    private final String REPRESENTATIVE = "xrb_164kohea8yrd57ehyh64t7o8wttmyxwuyyjnz4omrdpr8op7omptkiqe3693";

    private final NeuroClient api;
    private final String accountNumber;
    private final String privateKey;
    private final String publicKey;

    private SerialDisposable pendingTransactionsDisposable = new SerialDisposable();

    @Inject
    GetPendingBlocksUseCase(NeuroClient api, Realm realm) {
        this.api = api;
        this.accountNumber = provideAccountNumber(realm);
        this.privateKey = providePrivateKey(realm);
        this.publicKey = providePublicKey(realm);
    }

    String providePublicKey(Realm realm) {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getPublicKey();
        } else {
            return null;
        }
    }

    String provideAccountNumber(Realm realm) {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getAddressString();
        } else {
            return null;
        }
    }

    Credentials provideCredentials(Realm realm) {
        Credentials credentials = realm.where(Credentials.class).findFirst();
        return credentials;
    }

    String providePrivateKey(Realm realm) {
        Credentials credentials = provideCredentials(realm);
        if (credentials != null) {
            return credentials.getPrivateKey();
        } else {
            return null;
        }
    }


    public void startObservePendingTransactions() {
        if (pendingTransactionsDisposable == null) {
            pendingTransactionsDisposable = new SerialDisposable();
        }
        pendingTransactionsDisposable.set(
                Observable.interval(1, 15, TimeUnit.SECONDS)
                        .flatMap(aLong -> api.getPendingBlocks(new GetPendingBlocksRequest(accountNumber, "100"))
                                .onErrorReturnItem(new GetPendingBlocksResponse())
                                .flatMap(getPendingBlocksResponse -> {
                                    if (getPendingBlocksResponse.isEmpty())
                                        return Observable.empty();
                                    return Observable.just(getPendingBlocksResponse);
                                }).flatMapIterable(getPendingBlocksResponse -> getPendingBlocksResponse.blocks)
                                .flatMap(this::processedBlockBasedOnPendingHash))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object success) throws Exception {
                                System.out.println("success");
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(TAG, "onError: throwable: " + throwable.getMessage());
                                throwable.printStackTrace();
                            }
                        })
        );
    }

    private ObservableSource<Object> processedBlockBasedOnPendingHash(final String pendingBlock) {

        return api.getBlocksInfo(new GetBlocksInfoRequest(new String[]{pendingBlock}))
                .flatMap(blocksInfoResponse -> {

                    final String balance = blocksInfoResponse.getBalance();

                    return api.getAccountInfo(new AccountInfoRequest(accountNumber))
                            .flatMap(accountInfoResponse -> api.generateWork(new WorkRequest(accountInfoResponse.frontier))
                                    .flatMap(workResponse -> {

                                        String data = NOSUtil.computeStateHash(
                                                publicKey,
                                                accountInfoResponse.frontier,
//                                                "3F2A84D61991395BADFDF08EEEEEAC2D859E76B54B76DEC145E09652D9C15FF5",
                                                NOSUtil.addressToPublic(
                                                        REPRESENTATIVE
                                                ),
                                                balance,
                                                pendingBlock
                                        );
                                        String sign = NOSUtil.sign(privateKey, data);

                                        System.out.println("data: " + data);
                                        System.out.println("sign: " + sign);

                                        return api.process(new ProcessRequest(new ProcessBlock(
                                                accountNumber,
                                                accountInfoResponse.frontier,
//                                                "3F2A84D61991395BADFDF08EEEEEAC2D859E76B54B76DEC145E09652D9C15FF5",
                                                NOSUtil.addressToPublic(
                                                        REPRESENTATIVE
                                                ),
                                                balance,
                                                pendingBlock,
                                                sign,
                                                workResponse.work

                                        ))).map(any -> new Object());
                                    }));
                });
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
