package com.nytimes.android.external.register.di;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

final class AppSchedulerProvider implements SchedulerProvider {
    @Override 
    public Scheduler getMainThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override 
    public Scheduler getComputationThread() {
        return Schedulers.computation();
    }

    @Override 
    public Scheduler getIoThread() {
        return Schedulers.io();
    }
}
