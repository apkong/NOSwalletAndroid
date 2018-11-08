package co.nos.noswallet.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.nos.noswallet.NOSApplication;

public class FCMInstanceIdService extends FirebaseInstanceIdService {
    public static final String TAG = FCMInstanceIdService.class.getSimpleName();
    public static final String FCM_TOKEN = "FCM_TOKEN";

    public static void start(Context ctx) {
        ctx.startService(new Intent(ctx, FCMInstanceIdService.class));
    }

    //    @SuppressLint("ApplySharedPref")
//    @Override
//    public void onNewToken(String newToken) {
//        super.onNewToken(newToken);
//        Log.w(FCM_TOKEN, "onNewToken: " + newToken);
//    }
//
    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d(TAG, "onTokenRefresh() called");
        String newToken = obtainToken();
        if (newToken != null) {
            NOSApplication.getApplication(this).fcmTokenSubject.onNext(newToken);
        } else {
            NOSApplication.getApplication(this).fcmTokenSubject.onNext("");
        }
    }

    @Nullable
    private String obtainToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) {
            getSharedPreferences().edit().putString(FCM_TOKEN, token).commit();
        } else {
            token = getSharedPreferences().getString(FCM_TOKEN, null);
        }
        Log.d(TAG, "obtainToken() called, returning " + token);
        return token;
    }
}
