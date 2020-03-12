package com.nytimes.android.external.register.di

import android.app.Activity
import android.app.Service
import android.content.Context


class Injector {
    companion object {

        private var applicationComponent: RegisterApplicationComponent? = null

        @JvmStatic
        fun obtainAppComponent(context: Context): RegisterApplicationComponent {
            if (applicationComponent == null) {
                val applicationModule = RegisterApplicationModule(context)
                applicationComponent = DaggerRegisterApplicationComponent.builder()
                        .registerApplicationModule(applicationModule)
                        .build()
            }
            return applicationComponent!!
        }

        @JvmStatic
        fun create(activity: Activity): ActivityComponent {
            return obtainAppComponent(activity)
                    .plusActivityComponent(ActivityModule(activity))
        }

        @JvmStatic
        fun create(service: Service): ServiceComponent {
            return obtainAppComponent(service)
                    .plusServiceComponent(ServiceModule(service))
        }
    }
}
