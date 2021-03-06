package co.nos.noswallet.network.model.request.block;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.SerializedName;

import co.nos.noswallet.NOSUtil;
import co.nos.noswallet.model.Address;
import co.nos.noswallet.network.model.BlockTypes;
import co.nos.noswallet.util.NumberUtil;

/**
 * Send BlockItem
 */
@JsonPropertyOrder({
        "type",
        "previous",
        "account",
        "representative",
        "balance",
        "link",
        "work",
        "signature"
})
public class StateBlock extends Block {
    @JsonProperty("type")
    private String type;

    @JsonProperty("previous")
    private String previous;

    @JsonProperty("account")
    private String account;

    @JsonProperty("representative")
    private String representative;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("link")
    private String link;

    @JsonProperty("signature")
    private String signature;

    @SerializedName("sendAmount")
    private String sendAmount;

    private String privateKey;
    private String publicKey;

    public StateBlock() {
        this.type = BlockTypes.STATE.toString();
    }

    public StateBlock(BlockTypes blockType, String private_key, String previous,
                      String representative,
                      String balance, String link) {
        this.privateKey = private_key;
        this.publicKey = NOSUtil.privateToPublic(private_key);
        Address linkAddress = new Address(link);
        link = linkAddress.isValidAddress() ? NOSUtil.addressToPublic(linkAddress.getAddress()) : link;

        this.setInternal_block_type(blockType);
        this.type = BlockTypes.STATE.toString();
        this.previous = previous;
        this.account = NOSUtil.publicToAddress(publicKey);
        this.representative = representative;
        if (blockType == BlockTypes.SEND || blockType == BlockTypes.RECEIVE) {
            this.sendAmount = balance;
        } else {
            this.balance = balance;
        }
        this.link = link;

        if (this.balance != null) {
            sign();
        }
    }

    private void sign() {
        String hash = NOSUtil.computeStateHash(
                publicKey,
                previous,
                NOSUtil.addressToPublic(representative),
                NumberUtil.getRawAsHex(this.balance),
                link);
        this.signature = NOSUtil.sign(privateKey, hash);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
        sign();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String getWork() {
        return work;
    }

    @Override
    public void setWork(String work) {
        this.work = work;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @JsonIgnore
    public String getSendAmount() {
        return sendAmount;
    }

    public void setSendAmount(String sendAmount) {
        this.sendAmount = sendAmount;
    }
}
