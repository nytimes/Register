package com.nytimes.android.external.registerlib;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public final class JsonHelper {
    private static final String TAG = JsonHelper.class.getSimpleName();

    private JsonHelper() {
        // intentionally blank
    }

    public static String getFieldAsStringOrNull(JSONObject obj, String field) {
        return (obj == null) ? null : obj.optString(field, null);
    }

    public static int getFieldAsIntOrZero(JSONObject obj, String field) {
        return (obj == null) ? 0 : obj.optInt(field);
    }

    public static long getFieldAsLongOrZero(JSONObject obj, String field) {
        return (obj == null) ? 0L : obj.optLong(field);
    }

    public static void addToObjIfNotNull(String field, String val, JSONObject obj) {
        if (val != null) {
            try {
                obj.put(field, val);
            } catch (JSONException exc) {
                Log.e(TAG, "Error putting " + field + "," + val, exc);
            }
        }
    }

    public static void addToObj(String field, Integer val, JSONObject obj) {
        try {
            obj.put(field, val);
        } catch (JSONException exc) {
            Log.e(TAG, "Error putting " + field + "," + val, exc);
        }
    }

    public static void addToObj(String field, long val, JSONObject obj) {
        try {
            obj.put(field, val);
        } catch (JSONException exc) {
            Log.e(TAG, "Error putting " + field + "," + val, exc);
        }
    }
}
