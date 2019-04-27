package com.nytimes.android.external.register.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nytimes.android.external.register.model.GsonAdaptersConfig
import com.nytimes.android.external.register.model.GsonAdaptersConfigSku

object GsonFactory {

    @JvmStatic
    fun create(): Gson {
        return GsonBuilder()
                .registerTypeAdapterFactory(GsonAdaptersConfig())
                .registerTypeAdapterFactory(GsonAdaptersConfigSku())
                .create()
    }
}
