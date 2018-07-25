package co.nos.noswallet.network.nosModel;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class GetBlocksInfoResponse implements Serializable {

    @SerializedName("blocks")
    public Map<String, BlocksValue> blocks;

    private ProcessBlock processBlock;

    public static class BlocksValue {
        @SerializedName("block_account")
        public String block_account;

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
            Gson gson = new Gson();
            String key = this.blocks.keySet().iterator().next();
            BlocksValue first = this.blocks.get(key);
            processBlock = gson.fromJson(first.contents, ProcessBlock.class);
        }
        return processBlock;
    }

    public String getBalance() {
        return getProcessBlock().balance;
    }

}
