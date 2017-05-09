package com.nytimes.android.external.playbillingtester.di;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.PermissionHandler;
import com.nytimes.android.external.playbillingtester.R;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ImmutableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import dagger.Module;
import dagger.Provides;

import static com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases.CONFIG_FILE;

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
                return gson.fromJson(new FileReader(
                        new File(Environment.getExternalStorageDirectory().getPath(), CONFIG_FILE)), Config.class);
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
}
