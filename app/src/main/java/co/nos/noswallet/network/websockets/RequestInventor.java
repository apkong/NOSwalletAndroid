package co.nos.noswallet.network.websockets;

import android.support.annotation.NonNull;
import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.GetAccountHistoryRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetProofOfWorkRequest;
import co.nos.noswallet.network.websockets.model.ProcessBlockRequest;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class RequestInventor {
    public static final String TAG = RequestInventor.class.getSimpleName();

    private volatile String representative;
    private final String accountNumber, privateKey, publicKey;
    private volatile String accountBalance, accountFrontier;

    @Inject
    public RequestInventor(CredentialsProvider credentialsProvider) {
        accountNumber = credentialsProvider.provideAccountNumber();
        privateKey = credentialsProvider.providePrivateKey();
        publicKey = credentialsProvider.providePublicKey();
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getAccountInformation() {
        return new AccountInfoRequest(accountNumber).toString();
    }

    public String getPendingBlocks() {
        return new GetPendingBlocksRequest(accountNumber, "1", CryptoCurrency.NOLLAR).toString();
    }

    public String getAccountHistory() {
        return new GetAccountHistoryRequest(accountNumber, "100").toString();
    }

    public String getRepresentative() {
        return representative;
    }

    public String generateWork(String frontier) {
        return new GetProofOfWorkRequest(frontier).toString();
    }

    public String providePublicKey() {
        return publicKey;
    }

    public String processBlock(WebsocketMachine.PendingBlocksCredentialsBag pendingBlocksCredentialsBag) {
        String work = pendingBlocksCredentialsBag.work;
        String amount = pendingBlocksCredentialsBag.amount;
        String accountBalance = pendingBlocksCredentialsBag.accountBalance;
        String previousBlock = pendingBlocksCredentialsBag.previousBlock;
        String blockHash = pendingBlocksCredentialsBag.blockHash;
        String totalBalance = sumBigValues(amount, accountBalance);
        System.out.println("totalBalance = " + totalBalance);

        String dataToSign = NOSUtil.computeStateHash(
                providePublicKey(),
                previousBlock,
                NOSUtil.addressToPublic(representative),
                getRawAsHex(totalBalance),
                blockHash
        );

        String signatureFromData = NOSUtil.sign(privateKey, dataToSign);

        System.out.println("data: " + dataToSign);
        System.out.println("sign: " + signatureFromData);

        String json = new ProcessBlockRequest(accountNumber, previousBlock, totalBalance,
                blockHash, signatureFromData, work).toString();
        Log.w(TAG, "processing: " + json);
        return json;
    }

    private String sumBigValues(String balance, String accountBalance) {
        if (accountBalance == null || accountBalance.equals("0")) {
            return new BigDecimal(balance).toString();
        } else {
            Log.d(TAG, "sumBigValues() called with: balance = [" + balance + "], accountBalance = [" + accountBalance + "]");
            return new BigDecimal(balance).add(new BigDecimal(accountBalance)).toString();
        }
    }

    public static String getRawAsHex(@NonNull String raw) {
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

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String val) {
        accountBalance = val;
    }

    public void setAccountFrontier(String val) {
        accountFrontier = val;
    }

    public String getAccountFrontier() {
        return accountFrontier;
    }

    public String processSendCoinsBlock(WebsocketMachine.PendingSendCoinsCredentialsBag bag) {
        Log.d(TAG, "processSendCoinsBlock() called with: bag = [" + bag + "]");


        String totalBalance = NOSUtil.substractBigIntegers(accountBalance, bag.amount);
        String accountNumber = bag.accountNumber;
        String previousBlock = bag.frontier;
        String work = bag.work;
        String link = NOSUtil.addressToPublic(bag.destinationAccount);
        String dataToSign = NOSUtil.computeStateHash(
                publicKey, previousBlock,
                NOSUtil.addressToPublic(bag.representative),
                getRawAsHex(totalBalance),
                link
        );

        String signatureFromData = NOSUtil.sign(bag.privateKey, dataToSign);

        System.out.println("data: " + dataToSign);
        System.out.println("sign: " + signatureFromData);


        String json = new ProcessBlockRequest(accountNumber, previousBlock,
                totalBalance, link, signatureFromData, work)
                .toString();
        return json;
    }

//    public String sendCoins(String amount, String destinationAccount) {
//        return transferCoinsRequest(
//                accountNumber,
//                publicKey,
//                destinationAccount,
//                amount,
//                representative,
//                privateKey
//        );
//    }
//
//
//    private Observable<ProcessResponse> transferCoins(String sendingAccount,
//                                                      String publicKey,
//                                                      String destinationAccount,
//                                                      @Nonnull String amount,
//                                                      String totalBalance,
//                                                      String representative,
//                                                      String private_key,
//                                                      String frontier,
//                                                      String work) {
//
//    }

}
