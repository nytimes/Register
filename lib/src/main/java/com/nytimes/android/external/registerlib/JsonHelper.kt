package com.nytimes.android.external.registerlib

import android.util.Log

import org.json.JSONException
import org.json.JSONObject

object JsonHelper {
    private val TAG = JsonHelper::class.java.simpleName

    @JvmStatic
    fun getFieldAsStringOrNull(obj: JSONObject?, field: String): String? {
        var ret: String? = null
        if (obj != null) {
            try {
                ret = obj.getString(field)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error getting String $field", exc)
            }

        }
        return ret
    }

    @JvmStatic
    fun getFieldAsIntOrNull(obj: JSONObject?, field: String): Int? {
        var ret: Int? = null
        if (obj != null) {
            try {
                ret = obj.getInt(field)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error getting Int $field", exc)
            }

        }
        return ret
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
    fun addToObj(field: String, `val`: Int, obj: JSONObject) {
        try {
            obj.put(field, `val`)
        } catch (exc: JSONException) {
            Log.e(TAG, "Error putting $field,$`val`", exc)
        }

    }
}
