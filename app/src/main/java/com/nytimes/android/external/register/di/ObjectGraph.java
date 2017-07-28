package com.nytimes.android.external.register.di;

public interface ObjectGraph {

    ActivityComponent plusActivityComponent(ActivityModule activityModule);
    ServiceComponent plusServiceComponent(ServiceModule activityModule);

}
