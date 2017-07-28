package com.nytimes.android.external.playbillingtester.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nytimes.android.external.playbillingtester.APIOverrides;
import com.nytimes.android.external.playbillingtester.Purchases;
import com.nytimes.android.external.playbillingtester.R;
import com.nytimes.android.external.playbillingtester.Signer;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfig;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersConfigSku;
import com.nytimes.android.external.playbillingtester.model.GsonAdaptersRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import static com.nytimes.android.external.playbillingtester.APIOverrides.PREF_NAME;

@Module
public class ApplicationModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationModule.class);
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

    @Singleton
    @Provides
    @Named("gson_retrofit")
    Gson provideRetrofitGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapterFactory(new GsonAdaptersRepository())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Singleton
    @Provides
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .cache(null)
                .build();
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    APIOverrides provideAPIOverrides(SharedPreferences sharedPreferences) {
        return new APIOverrides(sharedPreferences);
    }

    @Provides
    @Singleton
    Purchases providePurchases(SharedPreferences sharedPreferences, Signer signer) {
        return new Purchases(sharedPreferences, signer);
    }

    @Provides
    @Singleton
    Optional<PrivateKey> providePrivateKey() {
        try {
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = application.getResources().openRawResource(R.raw.keystore);
            try {
                trusted.load(in, "playBillingTester".toCharArray());
            } finally {
                in.close();
            }
            return Optional.of((PrivateKey) trusted.getKey("playBillingTester", "playBillingTester".toCharArray()));
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                UnrecoverableKeyException exception) {
            LOGGER.error("Failed to provide private key", exception);
            return Optional.absent();
        }
    }

    @Provides
    @Singleton
    Optional<Signature> provideSignature() {
        try {
            return Optional.of(Signature.getInstance("SHA1withRSA", "BC"));
        } catch (NoSuchAlgorithmException | NoSuchProviderException exception) {
            LOGGER.error("Failed to provide signature", exception);
            return Optional.absent();
        }
    }

    @Provides
    @Singleton
    Signer provideSigner(@Nullable Optional<PrivateKey> privateKey, @Nullable Optional<Signature> signature) {
        return new Signer(privateKey, signature);
    }

    @Provides
    @Singleton
    SchedulerProvider provideScheduler(){
        return new AppSchedulerProvider();
    }
}
