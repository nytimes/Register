package com.nytimes.android.external.registerlib

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

/**
 * Utility class added to address API 21 issue with calling service with implicit intent
 *
 */
object ServiceIntentHelper {

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
    fun createExplicitFromImplicitIntent(context: Context, implicitIntent: Intent): Intent? {
        val resolveInfo = getResolveInfo(context, implicitIntent) ?: return null

        val component = getComponentFromServiceInfo(resolveInfo)

        return makeExplicitIntent(implicitIntent, component)
    }

    // Retrieve service that can match the given intent
    private fun getResolveInfo(context: Context, implicitIntent: Intent): ResolveInfo? {
        val packageManager = context.packageManager
        val resolveInfoList = packageManager.queryIntentServices(implicitIntent, 0)

        // Make sure only one match was found
        return if (resolveInfoList == null || resolveInfoList.size != 1) {
            null
        } else resolveInfoList[0]
    }

    // Get component info and create ComponentName
    private fun getComponentFromServiceInfo(serviceInfo: ResolveInfo): ComponentName {
        val packageName = serviceInfo.serviceInfo.packageName
        val className = serviceInfo.serviceInfo.name
        return ComponentName(packageName, className)
    }

    private fun makeExplicitIntent(implicitIntent: Intent, component: ComponentName): Intent {
        // Create a new intent. Use the old one for extras and such reuse
        val explicitIntent = Intent(implicitIntent)

        // Set the component to be explicit
        explicitIntent.component = component

        return explicitIntent
    }
}
