package com.nytimes.android.external.registerlib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Utility class added to address API 21 issue with calling service with implicit intent
 *
 */
public final class ServiceIntentHelper {

    private ServiceIntentHelper() {
        // utility class
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        ResolveInfo resolveInfo = getResolveInfo(context, implicitIntent);
        if (resolveInfo == null) {
            return null;
        }

        ComponentName component =  getComponentFromServiceInfo(resolveInfo);

        return makeExplicitIntent(implicitIntent, component);
    }

    // Retrieve service that can match the given intent
    private static ResolveInfo getResolveInfo(Context context, Intent implicitIntent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfoList == null || resolveInfoList.size() != 1) {
            return null;
        }
        return resolveInfoList.get(0);
    }

    // Get component info and create ComponentName
    private static ComponentName getComponentFromServiceInfo(ResolveInfo serviceInfo) {
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        return new ComponentName(packageName, className);
    }

    private static Intent makeExplicitIntent(Intent implicitIntent, ComponentName component) {
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
