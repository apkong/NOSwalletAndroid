package co.nos.noswallet.network.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import co.nos.noswallet.MainActivity;
import co.nos.noswallet.NOSApplication;
import co.nos.noswallet.R;

public class NosNotifier {

    public static final String ACTION_GOT_SAUCE = "ACTION_GOT_SAUCE";
    public static final String EXTRA_POSITION = "EXTRA_POSITION";
    public static final String FUNDS = "FUNDS";


    public static void showNotification(String _subText,
                                        String _bigContentTitle,
                                        String _bigText,
                                        int positionOnViewPager) {
        Context context = NOSApplication.get();
        String channelId = "NOS.cash";

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);

        notificationBuilder.setAutoCancel(true);

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(ACTION_GOT_SAUCE);
        intent.putExtra(EXTRA_POSITION, positionOnViewPager);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(_bigText);
        bigText.setBigContentTitle(_bigContentTitle);
        //bigText.setSummaryText("");


        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        //notificationBuilder.setContentTitle("");
        //notificationBuilder.setContentText("");
        notificationBuilder.setSubText(_subText);

        notificationBuilder.setPriority(Notification.PRIORITY_MAX);
        notificationBuilder.setStyle(bigText);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (notificationManager != null) {
            ID = (++ID) % 255;
            notificationManager.notify(ID, notificationBuilder.build());
        }
    }

    private static int ID = 0;
}
