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
        String ret = null;
        if (obj != null) {
            try {
                ret = obj.getString(field);
            } catch (JSONException exc) {
                Log.e(TAG, "Error getting String " + field, exc);
            }
        }
        return ret;
    }

    public static Integer getFieldAsIntOrNull(JSONObject obj, String field) {
        Integer ret = null;
        if (obj != null) {
            try {
                ret = obj.getInt(field);
            } catch (JSONException exc) {
                Log.e(TAG, "Error getting Int " + field, exc);
            }
        }
        return ret;
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
}
