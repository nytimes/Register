package com.nytimes.android.external.register

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED

class PermissionHandler {

    companion object {

        const val PERMISSION_REQ_CODE = 43242

        @JvmStatic
        fun handlePermissionResult(requestCode: Int, activity: Activity, vararg grantResults: Int) {
            if (requestCode == PERMISSION_REQ_CODE &&
                    grantResults.isNotEmpty() &&
                    grantResults[0] == PERMISSION_GRANTED) {
                val intent = activity.intent
                activity.finish()
                activity.startActivity(intent)
            }
        }

        @JvmStatic
        fun hasPermission(context: Context): Boolean {
            return PermissionChecker.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED
        }

        @JvmStatic
        fun requestPermission(activity: Activity) {
            ActivityCompat.requestPermissions(activity, arrayOf(READ_EXTERNAL_STORAGE), PERMISSION_REQ_CODE)
        }
    }

}
