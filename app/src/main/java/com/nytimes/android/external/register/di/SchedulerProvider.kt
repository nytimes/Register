package com.nytimes.android.external.register.di

import io.reactivex.Scheduler

/**
 * Helper interface to provide [Scheduler] for Rx calls. Extracting scheduler in a interace
 * allows us to inject [Scheduler]s for testing.
 */
interface SchedulerProvider {

    fun getMainThread() : Scheduler

    fun getComputationThread() : Scheduler

    fun getIoThread() : Scheduler

}
