package com.nytimes.android.external.register.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.common.base.Optional
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.APIOverrides.Companion.PREF_NAME
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.R
import com.nytimes.android.external.register.Signer
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.inject.Named
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @Singleton
    internal fun provideApplication(): Application {
        return application
    }

    @Singleton
    @Provides
    internal fun provideGson(): Gson {
        return GsonBuilder().create()
    }

    @Singleton
    @Provides
    @Named(GSON_RETROFIT)
    internal fun provideRetrofitGson(): Gson {
        return GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
    }

    @Singleton
    @Provides
    internal fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(null)
                .build()
    }

    @Provides
    @Singleton
    internal fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    internal fun provideAPIOverrides(sharedPreferences: SharedPreferences): APIOverrides {
        return APIOverrides(sharedPreferences)
    }

    @Provides
    @Singleton
    internal fun providePurchases(sharedPreferences: SharedPreferences, signer: Signer): Purchases {
        return Purchases(sharedPreferences, signer)
    }

    @Provides
    @Singleton
    internal fun providePrivateKey(): Optional<PrivateKey> {
        return try {
            val trusted = KeyStore.getInstance("BKS")
            val keyStore = application.resources.openRawResource(R.raw.keystore)
            keyStore.use { trusted.load(it, "register".toCharArray()) }
            Optional.of(trusted.getKey("register", "register".toCharArray()) as PrivateKey)
        } catch (exception: KeyStoreException) {
            Log.e("ApplicationModule", "Failed to provide private key", exception)
            Optional.absent()
        } catch (exception: IOException) {
            Log.e("ApplicationModule", "Failed to provide private key", exception)
            Optional.absent()
        } catch (exception: NoSuchAlgorithmException) {
            Log.e("ApplicationModule", "Failed to provide private key", exception)
            Optional.absent()
        } catch (exception: CertificateException) {
            Log.e("ApplicationModule", "Failed to provide private key", exception)
            Optional.absent()
        } catch (exception: UnrecoverableKeyException) {
            Log.e("ApplicationModule", "Failed to provide private key", exception)
            Optional.absent()
        }
    }

    @Provides
    @Singleton
    internal fun provideSignature(): Optional<Signature> {
        return try {
            Optional.of(Signature.getInstance("SHA1withRSA", "BC"))
        } catch (exception: NoSuchAlgorithmException) {
            Log.e("ApplicationModule", "Failed to provide signature", exception)
            Optional.absent()
        } catch (exception: NoSuchProviderException) {
            Log.e("ApplicationModule", "Failed to provide signature", exception)
            Optional.absent()
        }
    }

    @Provides
    @Singleton
    internal fun provideSigner(privateKey: Optional<PrivateKey>?, signature: Optional<Signature>?): Signer {
        return Signer(privateKey, signature)
    }

    @Provides
    @Singleton
    internal fun provideScheduler(): SchedulerProvider {
        return AppSchedulerProvider()
    }

    companion object {
        const val GSON_RETROFIT = "gson_retrofit"
    }
}
