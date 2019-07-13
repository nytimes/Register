package com.nytimes.android.external.register.di;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;

public final class Injector {

    private static final String INJECTOR_APP = "INJECTOR_APP";
    private static final String INJECTOR_ACTIVITY = "INJECTOR_ACTIVITY";
    private static final String INJECTOR_SERVICE = "INJECTOR_SERVICE";

    private Injector() {
    }

    @SuppressLint("WrongConstant")
    public static ApplicationComponent obtainAppComponent(Context context) {
        //noinspection ResourceType
        return (ApplicationComponent) context.getApplicationContext().getSystemService(INJECTOR_APP);
    }

    @SuppressLint("WrongConstant")
    public static ActivityComponent obtainActivityComponent(Context context) {
        //noinspection ResourceType
        return (ActivityComponent) context.getSystemService(INJECTOR_ACTIVITY);
    }

    @SuppressLint("WrongConstant")
    public static ServiceComponent obtainServiceComponent(Context context) {
        //noinspection ResourceType
        return (ServiceComponent) context.getSystemService(INJECTOR_SERVICE);
    }

    public static boolean matchesActivity(String name) {
        return INJECTOR_ACTIVITY.equals(name);
    }

    public static boolean matchesService(String name) {
        return INJECTOR_SERVICE.equals(name);
    }

    public static boolean matchesApp(String name) {
        return INJECTOR_APP.equals(name);
    }

    public static ActivityComponent create(Activity activity) {
        return  Injector.obtainAppComponent(activity)
                .plusActivityComponent(new ActivityModule(activity));
    }
    public static ServiceComponent create(Service service) {
        return  Injector.obtainAppComponent(service)
                .plusServiceComponent(new ServiceModule(service));
    }
}
