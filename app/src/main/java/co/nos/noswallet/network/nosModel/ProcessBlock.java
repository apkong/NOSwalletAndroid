package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.nos.noswallet.NOSUtil;

public class ProcessBlock implements Serializable {
    @SerializedName("type")
    public String type = "state";

    @SerializedName("account")
    public String account;

    @SerializedName("previous")
    public String previous;

    @SerializedName("representative")
    public String representative;

    @SerializedName("balance")
    public String balance;

    @SerializedName("link")
    public String link;

    @SerializedName("signature")
    public String signature;

    @SerializedName("work")
    public String work;

    public ProcessBlock() {
    }

    public ProcessBlock(String account, String previous, String representative, String balance, String link, String signature, String work) {
        this.account = account;
        this.previous = previous;
        this.representative = representative;
        this.balance = balance;
        this.link = link;
        this.signature = signature;
        this.work = work;
    }

    @Override
    public String toString() {
        return "ProcessBlock{" +
                "type='" + type + '\'' +
                ", account='" + account + '\'' +
                ", previous='" + previous + '\'' +
                ", representative='" + representative + '\'' +
                ", balance='" + balance + '\'' +
                ", link='" + link + '\'' +
                ", signature='" + signature + '\'' +
                ", work='" + work + '\'' +
                '}';
    }
}
