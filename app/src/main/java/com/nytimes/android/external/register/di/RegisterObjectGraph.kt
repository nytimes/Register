package com.nytimes.android.external.register.di

interface RegisterObjectGraph {

    fun plusActivityComponent(activityModule: ActivityModule): ActivityComponent

    fun plusServiceComponent(activityModule: ServiceModule): ServiceComponent

}
