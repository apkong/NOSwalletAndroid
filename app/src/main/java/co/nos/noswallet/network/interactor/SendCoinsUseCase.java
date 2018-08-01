package co.nos.noswallet.network.interactor;

import android.util.Log;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.db.RepresentativesProvider;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.ProcessBlock;
import co.nos.noswallet.network.nosModel.ProcessRequest;
import co.nos.noswallet.network.nosModel.WorkRequest;
import io.reactivex.Observable;

import static co.nos.noswallet.network.interactor.GetPendingBlocksUseCase.getRawAsHex;

public class SendCoinsUseCase {

    public static final String TAG = SendCoinsUseCase.class.getSimpleName();

    private final NeuroClient api;
    private final String accountNumber;
    private final String privateKey;
    private final String publicKey;

    private final String representative;

    @Inject
    public SendCoinsUseCase(NeuroClient api,
                            CredentialsProvider provider,
                            RepresentativesProvider representativesProvider) {
        this.api = api;
        this.accountNumber = provider.provideAccountNumber();
        this.privateKey = provider.providePrivateKey();
        this.publicKey = provider.providePublicKey();
        this.representative = representativesProvider.provideRepresentative();
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
        return transferCoinsInFormat30(realAmount, destinationAcount);
    }

    public Observable<Object> transferCoinsInFormat30(String amount,
                                                      String destinationAccount) {
        return transferCoins(
                accountNumber,
                publicKey,
                destinationAccount,
                amount,
                representative,
                privateKey
        );
    }

    private Observable<Object> transferCoins(String sendingAccount,
                                             String publicKey,
                                             String destinationAccount,
                                             String amount,
                                             String representative,
                                             String private_key) {

        Log.d(TAG, "transferCoins() called with: " +
                "sendingAccount = [" + sendingAccount + "], " +
                "publicKey = [" + publicKey + "], " +
                "destinationAccount = [" + destinationAccount + "], " +
                "amount = [" + amount + "], " +
                "representative = [" + representative + "], " +
                "private_key = [" + private_key + "]");

        return api.getAccountInfo(new AccountInfoRequest(sendingAccount))
                .flatMap(accountInfoResponse -> {

                    System.out.println("Account info response: " + accountInfoResponse);

                    String accountBalance = accountInfoResponse.balance;

                    return api.generateWork(new WorkRequest(accountInfoResponse.frontier))
                            .flatMap(workResponse -> {
                                String totalBalance = NOSUtil.substractBigIntegers(accountBalance, amount);

                                System.out.println("totalBalance = " + totalBalance);

                                String link = NOSUtil.addressToPublic(destinationAccount);
                                System.out.println("link = " + link);

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
