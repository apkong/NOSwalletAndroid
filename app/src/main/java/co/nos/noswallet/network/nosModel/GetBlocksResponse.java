package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import co.nos.noswallet.util.S;

public class GetBlocksResponse implements Serializable {

    @SerializedName("blocks")
    public List<BlocksValue> blocks;

    private ProcessBlock processBlock;

    public static class BlocksValue {
        @SerializedName("block_account")
        public String block_account;

        @SerializedName("hash")
        public String hash;

        @SerializedName("amount")
        public String amount;

        @SerializedName("contents")
        public String contents;


        @Override
        public String toString() {
            return "ABlocks{" +
                    "block_account='" + block_account + '\'' +
                    ", amount='" + amount + '\'' +
                    ", contents='" + contents + '\'' +
                    '}';
        }
    }

    public ProcessBlock getProcessBlock() {
        if (processBlock == null) {
            BlocksValue first = getBlock();
            processBlock = S.GSON.fromJson(first.contents, ProcessBlock.class);
        }
        return processBlock;
    }

    public String getBalance() {
        return getProcessBlock().balance;
    }

    public String getAmount() {
        BlocksValue first = getBlock();
        return first.amount;
    }

    public boolean hasBlock() {
        return blocks != null && blocks.size() > 0;
    }

    public BlocksValue getBlock() {
        return blocks.get(0);
    }
}
