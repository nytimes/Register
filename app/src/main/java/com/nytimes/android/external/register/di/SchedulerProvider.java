package com.nytimes.android.external.register.di;

import io.reactivex.Scheduler;

/**
 * Helper interface to provide {@link Scheduler} for Rx calls. Extracting scheduler in a interace
 * allows us to inject {@link Scheduler}s for testing.
 */
public interface SchedulerProvider {
    Scheduler getMainThread();
    Scheduler getComputationThread();
    Scheduler getIoThread();
}
