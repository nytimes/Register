package com.nytimes.android.external.register.di

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context


class Injector {
    companion object {

        private var applicationComponent: ApplicationComponent? = null

        @SuppressLint("WrongConstant")
        @JvmStatic
        fun obtainAppComponent(context: Context): ApplicationComponent {
            if (applicationComponent == null) {
                val applicationModule = ApplicationModule(context)
                applicationComponent = DaggerApplicationComponent.builder()
                        .applicationModule(applicationModule)
                        .build()
            }
            return applicationComponent!!
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
