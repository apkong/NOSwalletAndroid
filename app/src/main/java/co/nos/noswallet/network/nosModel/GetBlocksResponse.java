package co.nos.noswallet.network.nosModel;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static co.nos.noswallet.network.websockets.WebsocketMachine.safeCast;

public class GetBlocksResponse implements Serializable {

    @SerializedName("blocks")
    public List<BlocksValue> blocks;

    private ProcessBlock processBlock;

    public GetBlocksResponse(List<BlocksValue> blocks) {
        this.blocks = blocks;
    }

    public GetBlocksResponse(BlocksValue block) {
        this.blocks = new ArrayList<>();
        this.blocks.add(block);
    }

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
            return "BlocksValue{" +
                    "block_account='" + block_account + '\'' +
                    ", amount='" + amount + '\'' +
                    ", contents='" + contents + '\'' +
                    '}';
        }
    }

    @Nullable
    public ProcessBlock getProcessBlock() {

        if (processBlock == null) {
            BlocksValue first = getBlock();
            if (first == null || first.block_account == null) return null;
            processBlock = safeCast(first.contents, ProcessBlock.class);
        }
        return processBlock;
    }

    public String getBalance() {
        if (getProcessBlock() == null) return "0";
        return getProcessBlock().balance;
    }

    public String getAmount() {
        BlocksValue first = getBlock();
        if (first == null)
            return "0";
        return first.amount;
    }

    public boolean hasBlock() {
        return blocks != null && blocks.size() > 0;
    }

    @Nullable
    public BlocksValue getBlock() {
        if (blocks == null) return null;
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    public boolean blocksValueInvalid() {
        ProcessBlock block = getProcessBlock();
        return block == null;
    }
}
