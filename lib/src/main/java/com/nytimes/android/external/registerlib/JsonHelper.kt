package com.nytimes.android.external.registerlib

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

object JsonHelper {
    private val TAG = JsonHelper::class.java.simpleName

    fun getFieldAsStringOrNull(obj: JSONObject?, field: String): String? {
        return obj?.optString(field, null)
    }

    fun getFieldAsIntOrZero(obj: JSONObject?, field: String): Int {
        return obj?.optInt(field) ?: 0
    }

    fun getFieldAsLongOrZero(obj: JSONObject?, field: String): Long {
        return obj?.optLong(field) ?: 0L
    }

    fun addToObjIfNotNull(field: String, `val`: String?, obj: JSONObject) {
        if (`val` != null) {
            try {
                obj.put(field, `val`)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error putting $field,$`val`", exc)
            }

        }
    }

    fun addToObj(field: String, `val`: Int?, obj: JSONObject) {
        try {
            obj.put(field, `val`)
        } catch (exc: JSONException) {
            Log.e(TAG, "Error putting $field,$`val`", exc)
        }

    }

    fun addToObj(field: String, `val`: Long, obj: JSONObject) {
        try {
            obj.put(field, `val`)
        } catch (exc: JSONException) {
            Log.e(TAG, "Error putting $field,$`val`", exc)
        }

    }
}
