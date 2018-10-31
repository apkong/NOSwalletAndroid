package co.nos.noswallet.network.websockets;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import co.nos.noswallet.util.S;

public class SafeCast {

    public static final String TAG = SafeCast.class.getSimpleName();

    public static <T> T safeCast(JsonElement json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            Log.e(TAG, "safeCast: ", x);
            return null;
        }
    }

    public static <T> T safeCast(String json, Class<T> klazz) {
        try {
            return S.GSON.fromJson(json, klazz);
        } catch (JsonSyntaxException x) {
            Log.e(TAG, "safeCast: " + klazz.getSimpleName() + " failed to cast json: " + json);
            return null;
        }
    }

    public static String safeGet(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return null;
        return e.getAsString();
    }

    public static String safeGetOr(JsonObject o, String key, String defaultValue) {
        String value = safeGet(o, key);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }
}
