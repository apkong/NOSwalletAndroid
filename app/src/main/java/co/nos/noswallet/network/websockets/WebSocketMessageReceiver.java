package co.nos.noswallet.network.websockets;

import io.reactivex.Observable;
import okio.ByteString;

public interface WebSocketMessageReceiver {

    Observable<ByteString> processByteStringMessages();

    Observable<String> processStringMessages();
}
