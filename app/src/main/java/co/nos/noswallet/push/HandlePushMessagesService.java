package co.nos.noswallet.push;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.nos.noswallet.network.notifications.NosNotifier;

public class HandlePushMessagesService extends FirebaseMessagingService {

    public static final String TAG = HandlePushMessagesService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.w(TAG, "onMessageReceived: " + remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : data.keySet()) {
            Log.w(TAG, "onMessageReceived: " + key + " : " + data.get(key));
            stringBuilder.append(key + " : " + data.get(key) + ", ");
        }
        NosNotifier.showNotification(this, stringBuilder.toString());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

}
