package co.nos.noswallet.network.websockets.model;

import co.nos.noswallet.network.websockets.WebsocketMachine;

public class PendingBlocksCredentialsBag {
    public String balance, amount;
    public String previousBlock, frontier;
    public String accountBalance;
    public String work;
    public String blockHash;

    public PendingBlocksCredentialsBag() {
    }

    public PendingBlocksCredentialsBag(PendingBlocksCredentialsBag previous) {
        this.balance = previous.balance;
        this.amount = previous.amount;
        this.previousBlock = previous.previousBlock;
        this.frontier = previous.frontier;
        this.accountBalance = previous.accountBalance;
        this.work = previous.work;
        this.blockHash = previous.blockHash;
    }

    public PendingBlocksCredentialsBag balance(String val) {
        this.balance = val;
        return this;
    }

    public PendingBlocksCredentialsBag amount(String val) {
        this.amount = val;
        return this;
    }

    public PendingBlocksCredentialsBag previousBlock(String val) {
        this.previousBlock = val;
        return this;
    }

    public PendingBlocksCredentialsBag frontier(String val) {
        this.frontier = val;
        return this;
    }


    public PendingBlocksCredentialsBag accountBalance(String val) {
        this.accountBalance = val;
        return this;
    }

    public PendingBlocksCredentialsBag proofOfWork(String val) {
        this.work = val;
        return this;
    }

    public PendingBlocksCredentialsBag blockHash(String val) {
        this.blockHash = val;
        return this;
    }


    public PendingBlocksCredentialsBag clear() {
        this.balance = null;
        this.amount = null;
        this.frontier = null;
        this.previousBlock = null;
        this.accountBalance = null;
        this.work = null;
        this.blockHash = null;
        return this;
    }
}
