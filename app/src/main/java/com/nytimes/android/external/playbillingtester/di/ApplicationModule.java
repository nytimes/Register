package com.nytimes.android.external.playbillingtester.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfig;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfigSku;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases.PREF_NAME;

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(@NonNull Application application) {
        this.application = application;
    }


    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Singleton
    @Provides
    Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new GsonAdaptersConfig())
                .registerTypeAdapterFactory(new GsonAdaptersConfigSku())
                .create();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    APIOverridesAndPurchases provideAPIOverridesAndPurchases(SharedPreferences sharedPreferences, Gson gson) {
        return new APIOverridesAndPurchases(sharedPreferences, gson);
    }

}
