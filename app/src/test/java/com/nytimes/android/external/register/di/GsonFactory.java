package com.nytimes.android.external.register.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nytimes.android.external.register.model.GsonAdaptersConfig;
import com.nytimes.android.external.register.model.GsonAdaptersConfigSku;

public final class GsonFactory {

    private GsonFactory() {
        // utility
    }

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new GsonAdaptersConfig())
                .registerTypeAdapterFactory(new GsonAdaptersConfigSku())
                .create();
    }
}
