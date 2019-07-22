package com.nytimes.android.external.register.di

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class AppSchedulerProvider : SchedulerProvider {

    override fun getMainThread(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun getComputationThread(): Scheduler {
        return Schedulers.computation()
    }

    override fun getIoThread(): Scheduler {
        return Schedulers.io()
    }
}
