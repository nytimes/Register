package com.nytimes.android.external.register.di

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context


class Injector {
    companion object {

        private const val INJECTOR_APP = "INJECTOR_APP"
        private const val INJECTOR_ACTIVITY = "INJECTOR_ACTIVITY"
        private const val INJECTOR_SERVICE = "INJECTOR_SERVICE"

        @SuppressLint("WrongConstant")
        @JvmStatic
        fun obtainAppComponent(context: Context): ApplicationComponent {
            return context.applicationContext.getSystemService(INJECTOR_APP) as ApplicationComponent
        }

        @SuppressLint("WrongConstant")
        @JvmStatic
        fun obtainActivityComponent(context: Context): ActivityComponent {
            return context.getSystemService(INJECTOR_ACTIVITY) as ActivityComponent
        }

        @SuppressLint("WrongConstant")
        @JvmStatic
        fun obtainServiceComponent(context: Context): ServiceComponent {
            return context.getSystemService(INJECTOR_SERVICE) as ServiceComponent
        }

        @JvmStatic
        fun matchesActivity(name: String): Boolean {
            return INJECTOR_ACTIVITY == name
        }

        @JvmStatic
        fun matchesService(name: String): Boolean {
            return INJECTOR_SERVICE == name
        }

        @JvmStatic
        fun matchesApp(name: String): Boolean {
            return INJECTOR_APP == name
        }

        @JvmStatic
        fun create(activity: Activity): ActivityComponent {
            return Injector.obtainAppComponent(activity)
                    .plusActivityComponent(ActivityModule(activity))
        }

        @JvmStatic
        fun create(service: Service): ServiceComponent {
            return Injector.obtainAppComponent(service)
                    .plusServiceComponent(ServiceModule(service))
        }
    }
}
