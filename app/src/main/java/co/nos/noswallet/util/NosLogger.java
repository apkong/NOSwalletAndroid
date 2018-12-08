package co.nos.noswallet.util;

import android.util.Log;

import co.nos.noswallet.BuildConfig;

public class NosLogger {


    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable);
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }
}
