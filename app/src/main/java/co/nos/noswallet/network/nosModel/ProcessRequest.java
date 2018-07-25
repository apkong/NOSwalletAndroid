package co.nos.noswallet.network.nosModel;

import com.google.gson.annotations.SerializedName;

public class ProcessRequest {
    @SerializedName("action")
    public String action = "process";

    @SerializedName("block")
    public String block;

    public ProcessRequest(ProcessBlock block) {
        this.block = createShittyJson(block);
    }

    private String createShittyJson(ProcessBlock block) {
        return "{\n    " +
                "\"type\": \"state\",\n" +
                "    \"account\": \"" + block.account + "\",\n" +
                "    \"previous\": \"" + block.previous + "\",\n" +
                "    \"representative\": \"" + block.representative + "\",\n" +
                "    \"balance\": \"" + block.balance + "\",\n" +
                "    \"link\": \"" + block.link + "\",\n" +
                "    \"signature\": \"" + block.signature + "\",\n" +
                "    \"work\": \"" + block.work + "\"\n}" +
                "\n";
    }
}
