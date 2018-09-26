package co.nos.noswallet.network.websockets;

import android.util.Log;

import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class NosNodeWebSocketListener extends WebSocketListener implements WebSocketMessageReceiver {

    public static final String TAG = NosNodeWebSocketListener.class.getSimpleName();

    private final Charset charset = Charset.forName("UTF-8");

    private final PublishSubject<String> stringMessagesSubject = PublishSubject.create();
    private final PublishSubject<ByteString> byteStringMessagesSubject = PublishSubject.create();

    @Inject
    public NosNodeWebSocketListener() {
        super();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        Log.d(TAG, "onOpen() called with: webSocket = [" + webSocket + "], response = [" + response + "]");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.d(TAG, "onMessage() called with: webSocket = [" + webSocket + "], text = [" + text + "]");
        System.out.println("onMessage -> [" + text + "]");
        stringMessagesSubject.onNext(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.d(TAG, "onMessage() called with: webSocket = [" + webSocket + "], bytes = [" + bytes + "]");
        System.out.println("onMessage -> [" + bytes.string(charset) + "]");
        byteStringMessagesSubject.onNext(bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.d(TAG, "onClosing() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.d(TAG, "onClosed() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Log.d(TAG, "onFailure() called with: webSocket = [" + webSocket + "], t = [" + t + "], response = [" + response + "]");
        stringMessagesSubject.onError(t);
        byteStringMessagesSubject.onError(t);
    }

    @Override
    public Observable<ByteString> processByteStringMessages() {
        return byteStringMessagesSubject;
    }

    @Override
    public Observable<String> processStringMessages() {
        return stringMessagesSubject;
    }
}
