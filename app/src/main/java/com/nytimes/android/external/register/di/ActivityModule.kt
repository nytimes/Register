package com.nytimes.android.external.register.di

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.google.gson.Gson
import com.nytimes.android.external.register.APIOverrides.Companion.CONFIG_FILE
import com.nytimes.android.external.register.BuildConfig
import com.nytimes.android.external.register.GithubApi
import com.nytimes.android.external.register.R
import com.nytimes.android.external.register.di.RegisterApplicationModule.Companion.GSON_RETROFIT
import com.nytimes.android.external.register.model.Config
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.inject.Named

@Module
class ActivityModule(private val activity: Activity) {

    @Provides
    @ScopeActivity
    internal fun provideActivity(): Activity {
        return activity
    }

    @Provides
    @ScopeActivity
    internal fun provideConfig(gson: Gson): Config? {
            try {
                return gson.fromJson(BufferedReader(InputStreamReader(readConfigFile(), Charset.forName("UTF-8"))), Config::class.java)
            } catch (exc: FileNotFoundException) {
                Log.e("ActivityModule", activity.getString(R.string.nyt_register_config_not_found), exc)
            }
        return null
    }

    private fun readConfigFile(): InputStream {
        return activity.resources.assets.open(CONFIG_FILE)
    }

    @Provides
    @ScopeActivity
    internal fun provideAlertDialogBuilder(activity: Activity): AlertDialog.Builder {
        return AlertDialog.Builder(activity)
    }

    @Provides
    @ScopeActivity
    internal fun provideRetrofit(client: OkHttpClient, @Named(GSON_RETROFIT) gson: Gson): GithubApi {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(GithubApi::class.java)
    }

}
