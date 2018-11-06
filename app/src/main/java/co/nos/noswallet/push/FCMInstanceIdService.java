package co.nos.noswallet.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

import co.nos.noswallet.NOSApplication;

public class FCMInstanceIdService extends FirebaseMessagingService {

    public static final String FCM_TOKEN = "FCM_TOKEN";

    public static void start(Context ctx) {
        ctx.startService(new Intent(ctx, FCMInstanceIdService.class));
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onNewToken(String newToken) {
        super.onNewToken(newToken);
        Log.w(FCM_TOKEN, "onNewToken: " + newToken);
        getSharedPreferences().edit().putString(FCM_TOKEN, newToken).commit();
        NOSApplication.getApplication(this).fcmTokenSubject.onNext(newToken);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
