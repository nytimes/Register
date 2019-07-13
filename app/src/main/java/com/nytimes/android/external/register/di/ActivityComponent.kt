package com.nytimes.android.external.register.di

import com.nytimes.android.external.register.BuyActivity
import com.nytimes.android.external.register.MainActivity
import com.nytimes.android.external.register.SettingsActivity

import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class])
@ScopeActivity
interface ActivityComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: BuyActivity)
    fun inject(activity: SettingsActivity)
}
