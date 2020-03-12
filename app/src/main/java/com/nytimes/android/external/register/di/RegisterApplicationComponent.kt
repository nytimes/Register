package com.nytimes.android.external.register.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RegisterApplicationModule::class])
interface RegisterApplicationComponent : RegisterObjectGraph
