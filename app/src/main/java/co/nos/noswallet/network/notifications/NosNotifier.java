package co.nos.noswallet.network.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;

public class NosNotifier {

    public static final String ACTION_GOT_SAUCE = "ACTION_GOT_SAUCE";


    public static void showNewIncomingTransfer() {
        System.out.println("showNewIncomingTransfer");
        Context context = NOSApplication.get();
        String message = context.getString(R.string.incoming_transfer);
        showNotification(context, message);
    }

    private static void showNotification(Context context, String message) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(ACTION_GOT_SAUCE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(context, context.getPackageName())
                .setSmallIcon(R.drawable.ic_receive)
                .setContentTitle("NOS.cash")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }
}
