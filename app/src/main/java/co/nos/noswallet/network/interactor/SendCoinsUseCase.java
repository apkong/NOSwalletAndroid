package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.model.PreconfiguredRepresentatives;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.ProcessBlock;
import co.nos.noswallet.network.nosModel.ProcessRequest;
import co.nos.noswallet.network.nosModel.WorkRequest;
import io.reactivex.Observable;

import static co.nos.noswallet.network.interactor.GetPendingBlocksUseCase.getRawAsHex;

public class SendCoinsUseCase {

    private final NeuroClient api;
    private final String accountNumber;
    private final String privateKey;
    private final String publicKey;

    @Inject
    public SendCoinsUseCase(NeuroClient api,
                            CredentialsProvider provider) {
        this.api = api;
        this.accountNumber = provider.provideAccountNumber();
        this.privateKey = provider.providePrivateKey();
        this.publicKey = provider.providePublicKey();
    }

    String format30(String amount) {
        StringBuilder sb = new StringBuilder(amount);
        for (int i = 0; i < 30; i++) {
            sb.append("0");
        }
        return sb.toString();
    }

    public Observable<Object> transferCoins(String amount, String destinationAcount) {
        String realAmount = format30(amount);
        return transferCoinsInFormat30(realAmount,destinationAcount);
    }

    public Observable<Object> transferCoinsInFormat30(String amount,
                                                      String destinationAccount) {
        return transferCoins(
                accountNumber,
                publicKey,
                destinationAccount,
                amount,
                PreconfiguredRepresentatives.getRepresentative(),
                privateKey
        );
    }

    private Observable<Object> transferCoins(String sendingAccount,
                                             String publicKey,
                                             String destinationAccount,
                                             String amount,
                                             String representative,
                                             String private_key) {

        return api.getAccountInfo(new AccountInfoRequest(sendingAccount))
                .flatMap(accountInfoResponse -> {

                    String accountBalance = accountInfoResponse.balance;

                    return api.generateWork(new WorkRequest(accountInfoResponse.frontier))
                            .flatMap(workResponse -> {
                                String totalBalance = NOSUtil.substractBigIntegers(accountBalance, amount);
                                System.out.println("totalBalance = " + totalBalance);

                                String link = NOSUtil.addressToPublic(destinationAccount);

                                String dataToSign = NOSUtil.computeStateHash(
                                        publicKey,
                                        accountInfoResponse.frontier,
                                        NOSUtil.addressToPublic(representative),
                                        getRawAsHex(totalBalance),
                                        link
                                );

                                String signatureFromData = NOSUtil.sign(private_key, dataToSign);

                                System.out.println("data: " + dataToSign);
                                System.out.println("sign: " + signatureFromData);

                                return api.process(new ProcessRequest(new ProcessBlock(
                                                sendingAccount,
                                                accountInfoResponse.frontier,
                                                representative,
                                                totalBalance,
                                                link,
                                                signatureFromData,
                                                workResponse.work)
                                        )
                                ).map(any -> new Object());
                            });
                });
    }
}
