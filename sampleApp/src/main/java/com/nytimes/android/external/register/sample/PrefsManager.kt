package com.nytimes.android.external.register.sample

import android.content.SharedPreferences

class PrefsManager(private val sharedPreferences: SharedPreferences) {

    val isUsingTestGoogleServiceProvider: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_TEST_PROVIDER, false)

    fun setUsingGoogleServiceProvider(isTest: Boolean) {
        val edit = sharedPreferences.edit()
        edit.putBoolean(KEY_IS_TEST_PROVIDER, isTest)
        edit.commit()
    }

    companion object {

        private val KEY_IS_TEST_PROVIDER = "isTestProvider"
    }

}
