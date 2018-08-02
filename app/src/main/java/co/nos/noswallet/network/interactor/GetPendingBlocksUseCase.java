package co.nos.noswallet.network.interactor;

import android.util.Log;

import java.math.BigDecimal;
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
import co.nos.noswallet.network.nosModel.ProcessResponse;
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
                        .subscribe(success -> System.out.println("success :  " + success.hash),
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Log.e(TAG, "onError: throwable: " + throwable.getMessage());
                                        throwable.printStackTrace();
                                    }
                                })
        );
    }

    private ObservableSource<ProcessResponse> processedBlockBasedOnPendingHash(final String blockHash) {

        return api.getBlocksInfo(new GetBlocksInfoRequest(new String[]{blockHash}))
                .flatMap(blocksInfoResponse -> {

                    final String balance = blocksInfoResponse.getBalance();

                    final String amount = blocksInfoResponse.getAmount();

                    System.out.println("blocksInfoResponse.balance = " + balance);

                    return api.getAccountInfo(new AccountInfoRequest(accountNumber))
                            .flatMap(accountInfoResponse -> {

                                String _accountBalance = accountInfoResponse.balance;
                                String _frontier = accountInfoResponse.frontier;

                                String _previousBlock = accountInfoResponse.frontier;

                                if (accountInfoResponse.isFreshAccount()) {
                                    _accountBalance = "0";
                                    _frontier = publicKey;
                                    _previousBlock = "0";
                                }

                                final String previousBlock = _previousBlock;
                                final String accountBalance = _accountBalance;
                                final String frontier = _frontier;

                                System.out.println("accountInfoResponse.balance = " + accountBalance);

                                return api.generateWork(new WorkRequest(frontier))
                                        .flatMap(workResponse -> {

                                            String totalBalance = sumBigValues(amount, accountBalance);
                                            System.out.println("totalBalance = " + totalBalance);

                                            String dataToSign = NOSUtil.computeStateHash(
                                                    publicKey,
                                                    previousBlock,
                                                    NOSUtil.addressToPublic(REPRESENTATIVE),
                                                    getRawAsHex(totalBalance),
                                                    blockHash
                                            );

                                            String signatureFromData = NOSUtil.sign(privateKey, dataToSign);

                                            System.out.println("data: " + dataToSign);
                                            System.out.println("sign: " + signatureFromData);

                                            return api.process(new ProcessRequest(new ProcessBlock(
                                                    accountNumber,
                                                    previousBlock,
                                                    REPRESENTATIVE,
                                                    totalBalance,
                                                    blockHash,
                                                    signatureFromData,
                                                    workResponse.work)
                                            )).onErrorReturnItem(new ProcessResponse());
                                        });
                            });
                });
    }

    private String sumBigValues(String balance, String accountBalance) {
        return new BigDecimal(balance).add(new BigDecimal(accountBalance)).toString();
    }

    public static String getRawAsHex(String raw) {
        // convert to hex

        String hex = new BigInteger(raw.split("\\.")[0]).toString(16);

        // left-pad with zeros to be 32 length
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = 32 - hex.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }
        sb.append(hex);
        return sb.toString().toUpperCase();
    }


}
