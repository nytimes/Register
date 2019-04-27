package com.nytimes.android.external.register

import com.nytimes.android.external.register.di.SchedulerProvider

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.schedulers.ExecutorScheduler

class TestSchedulerProvider : SchedulerProvider {

    private val immediate = object : Scheduler() {
        override fun createWorker(): Scheduler.Worker {
            return ExecutorScheduler.ExecutorWorker { it.run() }
        }
    }

    override fun getMainThread(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun getComputationThread(): Scheduler {
        return immediate
    }

    override fun getIoThread(): Scheduler {
        return immediate
    }
}
