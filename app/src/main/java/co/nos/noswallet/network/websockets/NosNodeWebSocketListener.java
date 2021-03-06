package co.nos.noswallet.network.websockets;

import android.util.Log;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;

import co.nos.noswallet.util.NosLogger;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class NosNodeWebSocketListener extends WebSocketListener implements WebSocketMessageReceiver {

    private AtomicBoolean firstTime = new AtomicBoolean(false);

    public interface Doable {
        void onOpen(WebSocket websocket);
    }

    public interface IterableDoable {
        void onOpen(int index, WebSocket websocket);
    }

    public static final String TAG = NosNodeWebSocketListener.class.getSimpleName();

    private final Charset charset = Charset.forName("UTF-8");

    private final PublishSubject<String> stringMessagesSubject = PublishSubject.create();
    private final PublishSubject<ByteString> byteStringMessagesSubject = PublishSubject.create();

    @Nullable
    private Doable onOpenCallback;

    @Nullable
    private IterableDoable onOpenIterableCallback;
    private int tasksSize;

    @Inject
    public NosNodeWebSocketListener() {
        super();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        NosLogger.d(TAG, "onOpen() called with: webSocket = [" + webSocket + "], response = [" + response + "]");
        if (onOpenCallback != null) {
            onOpenCallback.onOpen(webSocket);
        }
        if (onOpenIterableCallback != null) {
            for (int index = 0; index < tasksSize; index++) {
                onOpenIterableCallback.onOpen(index, webSocket);
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        NosLogger.d(TAG, "onMessage: " + text);
        stringMessagesSubject.onNext(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        NosLogger.d(TAG, "onMessage() called with: webSocket = [" + webSocket + "], bytes = [" + bytes + "]");
        System.out.println("onMessage -> [" + bytes.string(charset) + "]");
        byteStringMessagesSubject.onNext(bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        NosLogger.d(TAG, "onClosing() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        NosLogger.d(TAG, "onClosed() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        NosLogger.d(TAG, "onFailure() called with: webSocket = [" + webSocket + "], t = [" + t + "], response = [" + response + "]");
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

    public WebSocketListener doOnOpen(Doable openable) {
        this.onOpenCallback = openable;
        return this;
    }

    public WebSocketListener doOnOpen
            (int tasksSize, IterableDoable openable) {
        this.onOpenIterableCallback = openable;
        this.tasksSize = tasksSize;
        return this;
    }
}
