package co.nos.noswallet.network.websockets;

import co.nos.noswallet.network.nosModel.GetPendingBlocksRequest;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;

public class WebsocketExecutor {

    public static final String TAG = WebsocketExecutor.class.getSimpleName();

    private final OkHttpClient client;
    private final String endpoint;
    private final NosNodeWebSocketListener listener;

    private WebSocket webSocket;

    public WebsocketExecutor(OkHttpClient client,
                             String endpoint,
                             NosNodeWebSocketListener listener) {
        this.client = client;
        this.endpoint = endpoint;
        this.listener = listener;
    }

    public WebSocket init(int tasksSize, NosNodeWebSocketListener.IterableDoable started) {
        Request request = new Request.Builder().url(endpoint).build();
        webSocket = client.newWebSocket(request, listener.doOnOpen(tasksSize, started));
        client.dispatcher().executorService().shutdown();
        return webSocket;
    }

    public WebSocket init(NosNodeWebSocketListener.Doable started) {
        Request request = new Request.Builder().url(endpoint).build();
        webSocket = client.newWebSocket(request, listener.doOnOpen(started));
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
        if (request == null) return;
        send(request, webSocket);
    }

    public <T> void send(T request, WebSocket webSocket) {

        String dataToSend = String.valueOf(request);
        if (dataToSend != null) {
            System.out.println("sending [" + dataToSend + "]");
            webSocket.send(dataToSend);
        }
    }
}
