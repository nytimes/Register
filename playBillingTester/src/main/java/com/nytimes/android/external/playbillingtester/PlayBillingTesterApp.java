package com.nytimes.android.external.playbillingtester;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.nytimes.android.external.playbillingtester.di.ApplicationComponent;
import com.nytimes.android.external.playbillingtester.di.ApplicationModule;
import com.nytimes.android.external.playbillingtester.di.DaggerApplicationComponent;
import com.nytimes.android.external.playbillingtester.di.Injector;

public class PlayBillingTesterApp extends Application {

    protected ApplicationComponent applicationComponent;
    protected ApplicationModule applicationModule;

    @NonNull
    public static PlayBillingTesterApp get(@NonNull Context context) {
        return (PlayBillingTesterApp) context.getApplicationContext();
    }

    protected void initDagger() {
        applicationModule = new ApplicationModule(this);
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(applicationModule)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initDagger();
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesApp(name)) {
            return applicationComponent;
        }
        return super.getSystemService(name);
    }
}
