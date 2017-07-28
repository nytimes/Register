package com.nytimes.android.external.register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public final class PermissionHandler {

    static final int PERMISSION_REQ_CODE = 43242;

    private PermissionHandler() {
        // utility class
    }

    static void handlePermissionResult(int requestCode, Activity activity, int... grantResults) {
        if (requestCode == PERMISSION_REQ_CODE && grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
            Intent intent = activity.getIntent();
            activity.finish();
            activity.startActivity(intent);
        }
    }

    public static boolean hasPermission(Context context) {
        return PermissionChecker.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
    }
}
