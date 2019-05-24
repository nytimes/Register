package com.nytimes.android.external.register

import android.app.Application
import com.nytimes.android.external.register.di.ApplicationComponent
import com.nytimes.android.external.register.di.ApplicationModule
import com.nytimes.android.external.register.di.DaggerApplicationComponent
import com.nytimes.android.external.register.di.Injector

class RegisterApp : Application() {

    private lateinit var applicationComponent: ApplicationComponent
    private lateinit var applicationModule: ApplicationModule

    override fun onCreate() {
        super.onCreate()
        initDagger()
    }

    private fun initDagger() {
        applicationModule = ApplicationModule(this)
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(applicationModule)
                .build()
    }

    override fun getSystemService(name: String): Any? {
        return if (Injector.matchesApp(name)) {
            applicationComponent
        } else super.getSystemService(name)
    }

}
