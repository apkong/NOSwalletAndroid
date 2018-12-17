package co.nos.noswallet.network.websockets.model;

public class PendingSendCoinsCredentialsBag {
    public String accountNumber, publicKey, amount;
    public String representative, privateKey, frontier;
    public String accountBalance, destinationAccount, work;

    @Override
    public String toString() {
        return "{" +
                "accountNumber='" + accountNumber + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", amount='" + amount + '\'' +
                ", representative='" + representative + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", frontier='" + frontier + '\'' +
                ", accountBalance='" + accountBalance + '\'' +
                ", destinationAccount='" + destinationAccount + '\'' +
                ", work='" + work + '\'' +
                '}';
    }

    public PendingSendCoinsCredentialsBag() {

    }

    public PendingSendCoinsCredentialsBag(PendingSendCoinsCredentialsBag previousBag) {
        accountNumber = previousBag.accountNumber;
        publicKey = previousBag.publicKey;
        amount = previousBag.amount;
        representative = previousBag.representative;
        privateKey = previousBag.privateKey;
        frontier = previousBag.frontier;
        destinationAccount = previousBag.destinationAccount;
        accountBalance = previousBag.accountBalance;
        work = previousBag.work;
    }

    public PendingSendCoinsCredentialsBag withAccountNumber(String val) {
        accountNumber = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withPublicKey(String val) {
        publicKey = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withAmount(String val) {
        amount = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withDestinationAccount(String val) {
        destinationAccount = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withRepresentative(String val) {
        representative = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withPrivateKey(String val) {
        privateKey = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withFrontier(String val) {
        frontier = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withAccountBalance(String val) {
        accountBalance = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag withProofOfWork(String val) {
        work = val;
        return this;
    }

    public PendingSendCoinsCredentialsBag clear() {
        accountNumber
                = publicKey
                = amount
                = representative
                = privateKey
                = frontier
                = accountBalance
                = work
                = destinationAccount = null;
        return this;
    }
}
