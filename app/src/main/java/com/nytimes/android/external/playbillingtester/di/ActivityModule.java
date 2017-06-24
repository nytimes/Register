package com.nytimes.android.external.playbillingtester.di;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.BuildConfig;
import com.nytimes.android.external.playbillingtester.GithubApi;
import com.nytimes.android.external.playbillingtester.PermissionHandler;
import com.nytimes.android.external.playbillingtester.R;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ImmutableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.common.base.Charsets.UTF_8;
import static com.nytimes.android.external.playbillingtester.APIOverrides.CONFIG_FILE;

@Module
public class ActivityModule {

    static final Logger LOGGER = LoggerFactory.getLogger(ActivityModule.class);

    private final Activity activity;

    public ActivityModule(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ScopeActivity
    Activity provideActivity() {
        return activity;
    }


    @Provides
    @ScopeActivity
    Config provideConfig(Gson gson) {
        if (PermissionHandler.hasPermission(activity)) {
            try {
                return gson.fromJson(Files.newReader(new File(Environment.getExternalStorageDirectory().getPath(),
                        CONFIG_FILE), UTF_8), Config.class);
            } catch (FileNotFoundException exc) {
                LOGGER.error(activity.getString(R.string.config_not_found), exc);
            }
        } else {
            PermissionHandler.requestPermission(activity);
        }
        return ImmutableConfig.builder().build();
    }

    @Provides
    @ScopeActivity
    AlertDialog.Builder provideAlertDialogBuilder(Activity activity) {
        return new AlertDialog.Builder(activity);
    }

    @Provides
    @ScopeActivity
    GithubApi provideRetrofit(OkHttpClient client, @Named("gson_retrofit") Gson gson) {
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.GITHUB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(GithubApi.class);
    }

}
