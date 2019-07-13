package com.nytimes.android.external.register.di

interface ObjectGraph {

    fun plusActivityComponent(activityModule: ActivityModule): ActivityComponent

    fun plusServiceComponent(activityModule: ServiceModule): ServiceComponent

}
