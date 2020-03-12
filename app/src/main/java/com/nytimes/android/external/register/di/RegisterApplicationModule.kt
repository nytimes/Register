package com.nytimes.android.external.register.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
class RegisterApplicationModule(private val context: Context) {

    @Provides
    @Singleton
    internal fun provideContext(): Context {
        return context
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
    internal fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
    internal fun providePrivateKey(): PrivateKey? {
        return try {
            val trusted = KeyStore.getInstance("BKS")
            val keyStore = context.resources.openRawResource(R.raw.register_keystore)
            keyStore.use { trusted.load(it, "register".toCharArray()) }
            trusted.getKey("register", "register".toCharArray()) as PrivateKey
        } catch (exception: KeyStoreException) {
            Log.e("RegisterApplicationModu", "Failed to provide private key", exception)
            null
        } catch (exception: IOException) {
            Log.e("RegisterApplicationModu", "Failed to provide private key", exception)
            null
        } catch (exception: NoSuchAlgorithmException) {
            Log.e("RegisterApplicationModu", "Failed to provide private key", exception)
            null
        } catch (exception: CertificateException) {
            Log.e("RegisterApplicationModu", "Failed to provide private key", exception)
            null
        } catch (exception: UnrecoverableKeyException) {
            Log.e("RegisterApplicationModu", "Failed to provide private key", exception)
            null
        }
    }

    @Provides
    @Singleton
    internal fun provideSignature(): Signature? {
        return try {
            Signature.getInstance("SHA1withRSA")
        } catch (exception: NoSuchAlgorithmException) {
            Log.e("RegisterApplicationModu", "Failed to provide signature", exception)
            null
        } catch (exception: NoSuchProviderException) {
            Log.e("RegisterApplicationModu", "Failed to provide signature", exception)
            null
        }
    }

    @Provides
    @Singleton
    internal fun provideSigner(privateKey: PrivateKey?, signature: Signature?): Signer {
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
