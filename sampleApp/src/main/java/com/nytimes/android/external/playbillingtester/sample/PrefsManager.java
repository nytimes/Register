package com.nytimes.android.external.playbillingtester.sample;

import android.content.SharedPreferences;

public class PrefsManager {

    private static final String KEY_IS_TEST_PROVIDER = "isTestProvider";
    private final SharedPreferences sharedPreferences;

    public PrefsManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isUsingTestGoogleServiceProvider() {
        return sharedPreferences.getBoolean(KEY_IS_TEST_PROVIDER, false);
    }

    public void setUsingGoogleServiceProvider(boolean isTest) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(KEY_IS_TEST_PROVIDER, isTest);
        edit.commit();
    }

}
