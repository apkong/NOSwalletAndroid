package co.nos.noswallet.network.interactor;

import javax.inject.Inject;

import co.nos.noswallet.db.CredentialsProvider;
import co.nos.noswallet.model.NOSWallet;
import co.nos.noswallet.network.NeuroClient;
import co.nos.noswallet.network.nosModel.AccountInfoRequest;
import io.reactivex.Observable;

public class CheckAccountBalanceUseCase {

    private final NeuroClient neuroClient;
    private final String accountNumber;
    private final NOSWallet nosWallet;

    @Inject
    CheckAccountBalanceUseCase(NeuroClient neuroClient,
                               CredentialsProvider provider,
                               NOSWallet nosWallet) {
        this.neuroClient = neuroClient;
        this.accountNumber = provider.provideAccountNumber();
        this.nosWallet = nosWallet;
    }

    public Observable<String> execute() {
        return neuroClient
                .getAccountInfo(new AccountInfoRequest(accountNumber))
                .map(response -> {
                    String accountBalance = response.balance;
                    String amount = NOSWallet.rawToNeuros(accountBalance);
                    nosWallet.setNeuros(amount);

                    return accountBalance;
                })
                .doOnNext(s -> {
                    System.out.println("s");
                    nosWallet.setRawAmount(s);
                });
    }
}