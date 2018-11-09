package co.nos.noswallet.push;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.network.notifications.NosNotifier;

public class HandlePushMessagesService extends FirebaseMessagingService {

    public static final String TAG = HandlePushMessagesService.class.getSimpleName();

    private Handler handler;

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public static void start(Context x) {
        x.startService(new Intent(x, HandlePushMessagesService.class));
    }

    private volatile Runnable runnable;

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (Looper.myLooper() == Looper.getMainLooper()) {
            processMessage(remoteMessage);
        } else {
            post(() -> processMessage(remoteMessage));
        }
    }

    private void post(Runnable runnable) {
        handler = getHandler();
        handler.removeCallbacks(this.runnable);
        this.runnable = runnable;
        handler.post(runnable);
    }

    private void processMessage(final RemoteMessage remoteMessage) {
        Log.w(TAG, "onMessageReceived: " + remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : data.keySet()) {
            Log.w(TAG, "onMessageReceived: " + key + " : " + data.get(key));
            stringBuilder.append(key + " : " + data.get(key) + ", ");
        }
        NosNotifier.showNotification(NOSApplication.get(), stringBuilder.toString());
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
