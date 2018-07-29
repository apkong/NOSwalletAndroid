package co.nos.noswallet.network.interactor;

import android.util.Log;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.db.RepresentativesProvider;
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

public class GetPendingBlocksUseCase {

    public static final String TAG = "GetPendingBlocksUseCase";

    private final NeuroClient api;
    private final String accountNumber;
    private final String privateKey;
    private final String publicKey;
    private final String REPRESENTATIVE;

    private SerialDisposable pendingTransactionsDisposable = new SerialDisposable();

    @Inject
    GetPendingBlocksUseCase(NeuroClient api,
                            CredentialsProvider provider,
                            RepresentativesProvider representativesProvider) {
        this.api = api;
        this.accountNumber = provider.provideAccountNumber();
        this.privateKey = provider.providePrivateKey();
        this.publicKey = provider.providePublicKey();
        this.REPRESENTATIVE = representativesProvider.provideRepresentative();
    }

    public void startObservePendingTransactions() {
        if (pendingTransactionsDisposable == null) {
            pendingTransactionsDisposable = new SerialDisposable();
        }
        pendingTransactionsDisposable.set(
                Observable.interval(1, 15, TimeUnit.SECONDS)
                        .flatMap(aLong -> api.getPendingBlocks(new GetPendingBlocksRequest(accountNumber, "100"))
                                .onErrorResumeNext(throwable -> {
                                    System.err.println(throwable.getCause());
                                    throwable.printStackTrace();
                                    return Observable.just(new GetPendingBlocksResponse());
                                })
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

    private ObservableSource<Object> processedBlockBasedOnPendingHash(final String blockHash) {

        return api.getBlocksInfo(new GetBlocksInfoRequest(new String[]{blockHash}))
                .flatMap(blocksInfoResponse -> {

                    final String balance = blocksInfoResponse.getBalance();

                    final String amount = blocksInfoResponse.getAmount();

                    System.out.println("blocksInfoResponse.balance = " + balance);

                    return api.getAccountInfo(new AccountInfoRequest(accountNumber))
                            .flatMap(accountInfoResponse -> {

                                String accountBalance = accountInfoResponse.balance;

                                System.out.println("accountInfoResponse.balance = " + accountBalance);

                                return api.generateWork(new WorkRequest(accountInfoResponse.frontier))
                                        .flatMap(workResponse -> {

                                            String totalBalance = sumBigValues(amount, accountBalance);
                                            System.out.println("totalBalance = " + totalBalance);

                                            String dataToSign = NOSUtil.computeStateHash(
                                                    publicKey,
                                                    accountInfoResponse.frontier,
                                                    NOSUtil.addressToPublic(REPRESENTATIVE),
                                                    getRawAsHex(totalBalance),
                                                    blockHash
                                            );

                                            String signatureFromData = NOSUtil.sign(privateKey, dataToSign);

                                            System.out.println("data: " + dataToSign);
                                            System.out.println("sign: " + signatureFromData);

                                            return api.process(new ProcessRequest(new ProcessBlock(
                                                            accountNumber,
                                                            accountInfoResponse.frontier,
                                                            REPRESENTATIVE,
                                                            totalBalance,
                                                            blockHash,
                                                            signatureFromData,
                                                            workResponse.work)
                                                    )
                                            ).map(any -> new Object());
                                        });
                            });
                });
    }

    private String sumBigValues(String balance, String accountBalance) {
        return new BigInteger(balance).add(new BigInteger(accountBalance)).toString();
    }

    public static String getRawAsHex(String raw) {
        // convert to hex
        String hex = new BigInteger(raw).toString(16);

        // left-pad with zeros to be 32 length
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = 32 - hex.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString().toUpperCase();
    }

    public static String getRawFromHex(String hex) {
        return new BigInteger(hex, 16).toString(10);
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
