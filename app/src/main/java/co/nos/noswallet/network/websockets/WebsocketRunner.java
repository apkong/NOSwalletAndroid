package co.nos.noswallet.network.websockets;

import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;

public class WebsocketRunner {

    public static final String TAG = WebsocketRunner.class.getSimpleName();

    public static final String ENDPOINT = "wss:/backendtest.nosnode.net:8888/";

    private final OkHttpClient client;
    private final String endpoint;
    private final NosNodeWebSocketListener listener;

    private WebSocket webSocket;

    public WebsocketRunner(OkHttpClient client,
                           String endpoint,
                           NosNodeWebSocketListener listener) {
        this.client = client;
        this.endpoint = endpoint;
        this.listener = listener;
    }

    public WebSocket init() {
        Request request = new Request.Builder().url(endpoint).build();
        webSocket = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
        return webSocket;
    }

    public Observable<String> observeMessages() {
        return listener.processStringMessages();
    }

    public Observable<ByteString> observeBinaryMessages() {
        return listener.processByteStringMessages();
    }


    public static void main(String[] args) {
        System.out.println("testing the mapping");
        GetPendingBlocksRequest request = new GetPendingBlocksRequest("123123213123", "1");
        System.out.println("\nrequest\n");
        System.out.println(request);
    }

    public <T> void send(T request) {
        if (request == null) {
            throw new RuntimeException(new NullPointerException("websocket request cannot be null"));
        }
        String dataToSend;
        webSocket.send(dataToSend = request.toString());
        System.out.println("[" + dataToSend + "] sent");
    }
}
