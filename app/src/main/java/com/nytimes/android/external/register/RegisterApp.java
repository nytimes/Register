package com.nytimes.android.external.register;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.nytimes.android.external.register.di.ApplicationComponent;
import com.nytimes.android.external.register.di.ApplicationModule;
import com.nytimes.android.external.register.di.DaggerApplicationComponent;
import com.nytimes.android.external.register.di.Injector;

public class RegisterApp extends Application {

    protected ApplicationComponent applicationComponent;
    protected ApplicationModule applicationModule;

    @NonNull
    public static RegisterApp get(@NonNull Context context) {
        return (RegisterApp) context.getApplicationContext();
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
