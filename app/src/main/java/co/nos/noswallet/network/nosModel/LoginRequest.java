package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginRequest implements Serializable {
//    {"action": "deterministic_key", "seed": "0000000000000000000000000000000000000000000000000000000000000000", "index": "1"}
    @SerializedName("action") public String action = "deterministic_key";
    @SerializedName("seed") public String seed;
    @SerializedName("index") public String index;

    public LoginRequest(String seed, String index) {
        this.seed = seed;
        this.index = index;
    }
}
