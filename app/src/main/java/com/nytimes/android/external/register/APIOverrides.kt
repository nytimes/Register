package com.nytimes.android.external.register

import android.content.SharedPreferences

import javax.inject.Inject

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

class APIOverrides @Inject constructor(private val sharedPreferences: SharedPreferences) {

    var isBillingSupportedResponse: Int
        get() = sharedPreferences.getInt(IS_BILLING_SUPPORTED, RESULT_DEFAULT)
        set(response) = sharedPreferences.edit().putInt(IS_BILLING_SUPPORTED, response).apply()

    var getBuyIntentResponse: Int
        get() = sharedPreferences.getInt(GET_BUY_INTENT, RESULT_DEFAULT)
        internal set(response) = sharedPreferences.edit().putInt(GET_BUY_INTENT, response).apply()

    var buyResponse: Int
        get() = sharedPreferences.getInt(BUY, RESULT_DEFAULT)
        set(response) = sharedPreferences.edit().putInt(BUY, response).apply()

    var getPurchasesResponse: Int
        get() = sharedPreferences.getInt(GET_PURCHASES, RESULT_DEFAULT)
        internal set(response) = sharedPreferences.edit().putInt(GET_PURCHASES, response).apply()
    var getSkuDetailsResponse: Int
        get() = sharedPreferences.getInt(GET_SKU_DETAILS, RESULT_DEFAULT)
        internal set(response) = sharedPreferences.edit().putInt(GET_SKU_DETAILS, response).apply()

    var consumePurchaseResponse: Int
        get() = sharedPreferences.getInt(CONSUME_PURCHASE, RESULT_DEFAULT)
        internal set(response) = sharedPreferences.edit().putInt(CONSUME_PURCHASE, response).apply()

    var getBuyIntentToReplaceSkusResponse: Int
        get() = sharedPreferences.getInt(GET_BUY_INTENT_TO_REPLACE_SKUS, RESULT_DEFAULT)
        internal set(response) = sharedPreferences.edit().putInt(GET_BUY_INTENT_TO_REPLACE_SKUS, response).apply()

    val replaceResponse: Int
        get() = sharedPreferences.getInt(REPLACE, RESULT_DEFAULT)

    val usersResponse: String
        get() = sharedPreferences.getString(USERS, DEFAULT_USER)

    fun setReplaceReponse(response: Int) {
        sharedPreferences.edit().putInt(REPLACE, response).apply()
    }

    fun setUsersReponse(user: String) {
        sharedPreferences.edit().putString(USERS, user).apply()
    }

    companion object {
        const val PREF_NAME = "RegisterModel"
        const val CONFIG_FILE = BuildConfig.CONFIGURATION_FILE_NAME
        const val RESULT_DEFAULT = -1            // - no user override
        private const val IS_BILLING_SUPPORTED = "isBillingSupported"
        private const val GET_BUY_INTENT = "getBuyIntent"
        private const val BUY = "Buy"
        private const val GET_PURCHASES = "getPurchases"
        private const val GET_SKU_DETAILS = "getSkuDetails"
        private const val CONSUME_PURCHASE = "consumePurchase"
        private const val GET_BUY_INTENT_TO_REPLACE_SKUS = "getBuyIntentToReplaceSkus"
        private const val REPLACE = "Replace"
        private const val USERS = "Users"
        private const val DEFAULT_USER = "200nyttest1@nytimes.com"
    }
}
