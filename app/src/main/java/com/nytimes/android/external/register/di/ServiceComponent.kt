package com.nytimes.android.external.register.di

import com.nytimes.android.external.register.BillingServiceStubImpl
import com.nytimes.android.external.register.RegisterService

import dagger.Subcomponent

@Subcomponent(modules = [ServiceModule::class])
@ScopeService
interface ServiceComponent {
    fun inject(service: RegisterService)
    fun inject(impl: BillingServiceStubImpl)
}
