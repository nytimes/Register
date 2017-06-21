package com.nytimes.android.external.playbillingtester;


import com.nytimes.android.external.playbillingtester.di.SchedulerProvider;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.schedulers.ExecutorScheduler;


public class TestSchedulerProvider implements SchedulerProvider {

    private final Scheduler immediate = new Scheduler() {
        @Override
        public Worker createWorker() {
            return new ExecutorScheduler.ExecutorWorker(Runnable::run);
        }
    };

    @Override
    public Scheduler getMainThread() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler getComputationThread() {
        return immediate;
    }

    @Override 
    public Scheduler getIoThread() {
        return immediate;
    }
}
