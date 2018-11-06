package co.nos.noswallet.network.websockets;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import co.nos.noswallet.network.nosModel.GetAccountHistoryRequest;
import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import co.nos.noswallet.network.nosModel.GetProofOfWorkRequest;
import co.nos.noswallet.network.websockets.model.PendingBlocksCredentialsBag;
import co.nos.noswallet.network.websockets.model.PendingSendCoinsCredentialsBag;
import co.nos.noswallet.network.websockets.model.ProcessBlockRequest;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import io.reactivex.annotations.Experimental;

public class RequestInventor {
    public static final String TAG = RequestInventor.class.getSimpleName();

    private final String privateKey, publicKey;

    private final Map<CryptoCurrency, String> accountNumbers = new HashMap<>();
    private final Map<CryptoCurrency, String> representatives = new HashMap<>();
    private final Map<CryptoCurrency, String> accountBalanceMap = new HashMap<>();
    private final Map<CryptoCurrency, String> accountFrontierMap = new HashMap<>();

    @Inject
    public RequestInventor(CredentialsProvider credentialsProvider) {
        for (CryptoCurrency cryptoCurrency : CryptoCurrency.values()) {
            accountNumbers.put(cryptoCurrency, credentialsProvider.provideAccountNumber(cryptoCurrency));
        }
        privateKey = credentialsProvider.providePrivateKey();
        publicKey = credentialsProvider.providePublicKey();
    }

    public void setRepresentative(String representative, CryptoCurrency cryptoCurrency) {
        representatives.put(cryptoCurrency, representative);
    }

    public String getAccountInformation(CryptoCurrency cryptoCurrency) {
        return new AccountInfoRequest(accountNumbers.get(cryptoCurrency), cryptoCurrency).toString();
    }

    public String getPendingBlocks(CryptoCurrency cryptoCurrency) {
        return new GetPendingBlocksRequest(accountNumbers.get(cryptoCurrency), "1", cryptoCurrency).toString();
    }

    public String getAccountHistory(CryptoCurrency cryptoCurrency) {
        return new GetAccountHistoryRequest(accountNumbers.get(cryptoCurrency), "100", cryptoCurrency).toString();
    }

    public String getRepresentative(CryptoCurrency currency) {
        return representatives.get(currency);
    }


    public String generateWork(String frontier, CryptoCurrency cryptoCurrency) {
        return new GetProofOfWorkRequest(frontier, cryptoCurrency).toString();
    }

    @Experimental
    public String generateWork(CryptoCurrency cryptoCurrency) {
        return new GetProofOfWorkRequest(accountFrontierMap.get(cryptoCurrency), cryptoCurrency).toString();
    }

    public String providePublicKey() {
        return publicKey;
    }

    @Deprecated
    public String processBlock(PendingBlocksCredentialsBag pendingBlocksCredentialsBag) {
        return processBlock(pendingBlocksCredentialsBag, CryptoCurrency.NOLLAR);
    }

    public String processBlock(PendingBlocksCredentialsBag pendingBlocksCredentialsBag, CryptoCurrency cryptoCurrency) {
        String work = pendingBlocksCredentialsBag.work;
        String amount = pendingBlocksCredentialsBag.amount;
        String accountBalance = pendingBlocksCredentialsBag.accountBalance;
        String previousBlock = pendingBlocksCredentialsBag.previousBlock;
        String blockHash = pendingBlocksCredentialsBag.blockHash;
        String totalBalance = sumBigValues(amount, accountBalance);
        System.out.println("totalBalance = " + totalBalance);
        if (blockHash == null) {
            return null;
        }

        String dataToSign = NOSUtil.computeStateHash(
                providePublicKey(),
                previousBlock,
                NOSUtil.addressToPublic(representatives.get(cryptoCurrency)),
                getRawAsHex(totalBalance),
                blockHash
        );

        String signatureFromData = NOSUtil.sign(privateKey, dataToSign);

        System.out.println("data: " + dataToSign);
        System.out.println("sign: " + signatureFromData);

        String json = new ProcessBlockRequest(accountNumbers.get(cryptoCurrency), previousBlock,
                totalBalance, blockHash, signatureFromData, work, cryptoCurrency)
                .withRepresentative(representatives.get(cryptoCurrency))
                .toString();
        Log.w(TAG, "processing: " + json);
        return json;
    }

    private String sumBigValues(@Nullable String balance, @Nullable String accountBalance) {
        Log.w(TAG, "sumBigValues: " + balance + " + " + accountBalance);
        if (accountBalance == null || accountBalance.equals("0")) {
            return new BigDecimal(balance).toString();
        } else {
            if (balance == null) {
                return sumBigValues(accountBalance, balance);
            }


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

    public String getAccountNumber(CryptoCurrency cryptoCurrency) {
        return accountNumbers.get(cryptoCurrency);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAccountBalance(CryptoCurrency cryptoCurrency) {
        return accountBalanceMap.get(cryptoCurrency);
    }

    public void setAccountBalance(String val, CryptoCurrency currency) {
        accountBalanceMap.put(currency, val);
    }

    public void setAccountFrontier(String val, CryptoCurrency cryptoCurrency) {
        accountFrontierMap.put(cryptoCurrency, val);
    }

    public String getAccountFrontier(CryptoCurrency cryptoCurrency) {
        return accountFrontierMap.get(cryptoCurrency);
    }

    public String processSendCoinsBlock(PendingSendCoinsCredentialsBag bag, CryptoCurrency currency) {
        Log.d(TAG, "processSendCoinsBlock() called with: bag = [" + bag + "]");

        String destinationAccount = bag.destinationAccount;
        String totalBalance = NOSUtil.substractBigIntegers(accountBalanceMap.get(currency), bag.amount);
        String accountNumber = bag.accountNumber;
        String previousBlock = bag.frontier;
        String work = bag.work;
        String link = NOSUtil.addressToPublic(destinationAccount);
        String representative = bag.representative;

        String dataToSign = NOSUtil.computeStateHash(
                publicKey, previousBlock,
                NOSUtil.addressToPublic(representative),
                getRawAsHex(totalBalance),
                link
        );

        String signatureFromData = NOSUtil.sign(bag.privateKey, dataToSign);

        System.out.println("data: " + dataToSign);
        System.out.println("sign: " + signatureFromData);

        String json = new ProcessBlockRequest(accountNumber, previousBlock,
                totalBalance, link, signatureFromData, work, currency)
                .withRepresentative(representative)
                .toString();
        return json;
    }
}
