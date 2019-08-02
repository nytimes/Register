package com.nytimes.android.external.registerlib

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

object JsonHelper {
    private val TAG = JsonHelper::class.java.simpleName

    @JvmStatic
    fun getFieldAsStringOrNull(obj: JSONObject?, field: String): String? {
        return obj?.optString(field, null)
    }

    @JvmStatic
    fun getFieldAsIntOrZero(obj: JSONObject?, field: String): Int {
        return obj?.optInt(field) ?: 0
    }

    @JvmStatic
    fun getFieldAsLongOrZero(obj: JSONObject?, field: String): Long {
        return obj?.optLong(field) ?: 0L
    }

    @JvmStatic
    fun addToObjIfNotNull(field: String, `val`: String?, obj: JSONObject) {
        if (`val` != null) {
            try {
                obj.put(field, `val`)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error putting $field,$`val`", exc)
            }

        }
    }

    @JvmStatic
    fun addToObj(field: String, `val`: Int?, obj: JSONObject) {
        try {
            obj.put(field, `val`)
        } catch (exc: JSONException) {
            Log.e(TAG, "Error putting $field,$`val`", exc)
        }

    }

    @JvmStatic
    fun addToObj(field: String, `val`: Long, obj: JSONObject) {
        try {
            obj.put(field, `val`)
        } catch (exc: JSONException) {
            Log.e(TAG, "Error putting $field,$`val`", exc)
        }

    }
}
