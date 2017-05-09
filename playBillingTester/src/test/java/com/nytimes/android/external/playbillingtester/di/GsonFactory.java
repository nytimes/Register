package com.nytimes.android.external.playbillingtester.di;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfig;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfigSku;

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
