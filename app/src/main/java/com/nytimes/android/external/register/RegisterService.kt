package com.nytimes.android.external.register

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.annotation.VisibleForTesting
import android.widget.Toast

import com.nytimes.android.external.register.di.Injector

import javax.inject.Inject

/**
 * Service that handles Play Billing Tester API calls
 */
open class RegisterService : Service() {

    @Inject
    lateinit var mBinder: IInAppBillingService.Stub

    override fun onCreate() {
        inject()
        super.onCreate()
        "RegisterService Created".makeToast()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        "RegisterService Started".makeToast()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        "RegisterService Destroyed".makeToast()
    }

    @VisibleForTesting
    open fun inject() {
        Injector.create(this).inject(this)
    }

    private fun String.makeToast() {
        Toast.makeText(this@RegisterService, this, Toast.LENGTH_SHORT).show()
    }

}


