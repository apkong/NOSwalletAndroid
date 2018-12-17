package co.nos.noswallet.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;
import java.util.Map;

import co.nos.noswallet.R;
import co.nos.noswallet.network.notifications.NosNotifier;
import co.nos.noswallet.network.websockets.currencyFormatter.CryptoCurrencyFormatter;
import co.nos.noswallet.persistance.currency.CryptoCurrency;
import co.nos.noswallet.util.NosLogger;

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
        NosLogger.w(TAG, "onMessageReceived: " + remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : data.keySet()) {
            NosLogger.w(TAG, "onMessageReceived: " + key + " : " + data.get(key));
            stringBuilder.append(key + " : " + data.get(key) + ", ");
        }

        String accountNumber = data.get("account");
        String amount = data.get("amount");
        String hash = data.get("hash");
        String block = data.get("hash");

        if (amount == null || amount.isEmpty()) return;

        Bundle bundle = new Bundle();

        bundle.putString(ACCOUNT, accountNumber);
        bundle.putString(AMOUNT, amount);
        bundle.putString(HASH, hash);
        bundle.putString(BLOCK, block);

        String currency = accountNumber.substring(0, 3);

        CryptoCurrency cryptoCurrency = CryptoCurrency.recognize(currency);
        if (accountNumber.charAt(4) == '_') {
            //dealing with nano?
            currency = accountNumber.substring(0, 4);
            cryptoCurrency = CryptoCurrency.recognize(currency);
        }
        formatter.useCurrency(cryptoCurrency);
        String X = formatter.rawtoUi(amount);

        X = formatWell(X);


        String CURRENCY = cryptoCurrency.name();

        String currency_received = getString(R.string.currency_received, CURRENCY);
        NosLogger.w(TAG, "processMessage: " + currency_received);

        String youReceivedText = getString(R.string.you_received_template, X, CURRENCY);

        NosNotifier.showNotification(currency_received,
                youReceivedText,
                getString(R.string.click_to_open_your_wallet),
                cryptoCurrency.getPosition());
    }

    public static String formatWell(String numberWithExtraZeros) {

        try {
            double d = Double.parseDouble(numberWithExtraZeros);

            if (d == (long) d) {
                return String.format(Locale.US, "%d", (long) d);
            } else {
                while (true) {
                    if (numberWithExtraZeros.charAt(numberWithExtraZeros.length() - 1) == '0') {
                        numberWithExtraZeros = numberWithExtraZeros.substring(0, numberWithExtraZeros.length() - 1);
                    } else return numberWithExtraZeros;
                }
            }
        } catch (Exception x) {
            return numberWithExtraZeros;
        }
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
