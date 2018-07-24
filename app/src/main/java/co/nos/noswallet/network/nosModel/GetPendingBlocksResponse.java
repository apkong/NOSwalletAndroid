package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import co.nos.noswallet.NOSUtil;

public class GetPendingBlocksResponse implements Serializable {

    @SerializedName("blocks")
    public ArrayList<String> blocks;

    public boolean isEmpty() {
        return NOSUtil.isEmpty(blocks);
    }
}
