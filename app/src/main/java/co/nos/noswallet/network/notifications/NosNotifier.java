package co.nos.noswallet.network.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;

public class NosNotifier {

    public static final String ACTION_GOT_SAUCE = "ACTION_GOT_SAUCE";
    public static final String FUNDS = "FUNDS";


    public static void showNewIncomingTransfer() {
        System.out.println("showNewIncomingTransfer");
        Context context = NOSApplication.get();
        String message = context.getString(R.string.incoming_transfer);
        showNotification(context, message, null);
    }

    public static void showNotification(Context context, String message, Bundle bundle) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(ACTION_GOT_SAUCE);
        if (bundle != null) {
            intent.putExtra(FUNDS, bundle);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                .Builder(context, context.getPackageName())
                .setSmallIcon(R.drawable.ic_receive)
                .setContentTitle("NOS.cash")
                .setContentText(message)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(++ID /* ID of notification */, notificationBuilder.build());
        }
    }

    public static void showNotification(String bigTextContent,
                                        String bigContentTitle,
                                        String bigContentDetail) {

        Context context = NOSApplication.get();

        String channelId = "NOS.cash";

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);

        notificationBuilder.setAutoCancel(true);

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(ACTION_GOT_SAUCE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(bigTextContent);
        bigText.setBigContentTitle(bigContentTitle);
        bigText.setSummaryText(bigContentDetail);

        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
//        mBuilder.setContentTitle("A");
//        mBuilder.setContentText("B");
        notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        notificationBuilder.setStyle(bigText);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelId, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (notificationManager != null) {
            ID = (++ID) % 255;
            notificationManager.notify(ID, notificationBuilder.build());
        }
    }

    static int ID = 0;
}
