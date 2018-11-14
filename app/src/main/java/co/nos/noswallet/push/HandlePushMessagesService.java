package co.nos.noswallet.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.nos.noswallet.network.notifications.NosNotifier;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;

public class HandlePushMessagesService extends FirebaseMessagingService {

    public static final String TAG = HandlePushMessagesService.class.getSimpleName();

    private final CryptoCurrencyFormatter formatter = new CryptoCurrencyFormatter();

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

        String accountNumber = data.get("account");
        String amount = data.get("amount");
        String hash = data.get("hash");
        String block = data.get("hash");

        Bundle bundle = new Bundle();

        bundle.putString(ACCOUNT, accountNumber);
        bundle.putString(AMOUNT, amount);
        bundle.putString(HASH, hash);
        bundle.putString(BLOCK, block);


        String currency = accountNumber.substring(0, 3);
        CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(currency);
        formatter.useCurrency(cryptoCurrency);
        String uiValue = formatter.rawtoUi(amount);
        String text = "You got " + uiValue + " " + cryptoCurrency.name();
        Log.w(TAG, "processMessage: " + text);

//        NosNotifier.showNotification(NOSApplication.get(), text, bundle);
        NosNotifier.showNotification("NOS.cash", text, "");
    }

    public static final String ACCOUNT = "account";
    public static final String AMOUNT = "amount";
    public static final String HASH = "hash";
    public static final String BLOCK = "block";

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }
}
