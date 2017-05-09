package com.nytimes.android.external.playbillingtester.di;


public interface ObjectGraph {

    ActivityComponent plusActivityComponent(ActivityModule activityModule);
    ServiceComponent plusServiceComponent(ServiceModule activityModule);

}
