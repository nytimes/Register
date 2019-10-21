package com.nytimes.android.external.register.di

import com.nytimes.android.external.register.MainFragment
import com.nytimes.android.external.register.SettingsFragment
import com.nytimes.android.external.register.buy.BuyFragment
import dagger.Subcomponent

@Subcomponent(modules = [ActivityModule::class])
@ScopeActivity
interface ActivityComponent {
    fun inject(activity: MainFragment)
    fun inject(activity: BuyFragment)
    fun inject(activity: SettingsFragment)
}
