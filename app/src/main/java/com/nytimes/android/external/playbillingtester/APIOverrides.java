package com.nytimes.android.external.playbillingtester;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import javax.inject.Inject;

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

public class APIOverrides {
    public static final String PREF_NAME = "PlayInApBillingModel";
    public static final String CONFIG_FILE = "playbillingtester.json";
    public static final int RESULT_DEFAULT = -1;            // - no user override
    private static final String IS_BILLING_SUPPORTED = "isBillingSupported";
    private static final String GET_BUY_INTENT = "getBuyIntent";
    private static final String BUY = "Buy";
    private static final String GET_PURCHASES = "getPurchases";
    private static final String GET_SKU_DETAILS = "getSkuDetails";
    private static final String CONSUME_PURCHASE = "consumePurchase";
    private static final String GET_BUY_INTENT_TO_REPLACE_SKUS = "getBuyIntentToReplaceSkus";
    private static final String REPLACE = "Replace";
    private static final String USERS = "Users";
    private static final String DEFAULT_USER = "200nyttest1@nytimes.com";

    @NonNull
    private final SharedPreferences sharedPreferences;

    @Inject
    public APIOverrides(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    int getIsBillingSupportedResponse() {
        return sharedPreferences.getInt(IS_BILLING_SUPPORTED, RESULT_DEFAULT);
    }

    void setIsBillingSupportedResponse(int response) {
        sharedPreferences.edit().putInt(IS_BILLING_SUPPORTED, response).apply();
    }

    public int getGetBuyIntentResponse() {
        return sharedPreferences.getInt(GET_BUY_INTENT, RESULT_DEFAULT);
    }

    void setGetBuyIntentResponse(int response) {
        sharedPreferences.edit().putInt(GET_BUY_INTENT, response).apply();
    }
    int getBuyResponse() {
        return sharedPreferences.getInt(BUY, RESULT_DEFAULT);
    }

    void setBuyResponse(int response) {
        sharedPreferences.edit().putInt(BUY, response).apply();
    }

    public int getGetPurchasesResponse() {
        return sharedPreferences.getInt(GET_PURCHASES, RESULT_DEFAULT);
    }

    void setGetPurchasesResponse(int response) {
        sharedPreferences.edit().putInt(GET_PURCHASES, response).apply();
    }
    public int getGetSkuDetailsResponse() {
        return sharedPreferences.getInt(GET_SKU_DETAILS, RESULT_DEFAULT);
    }

    void setGetSkuDetailsResponse(int response) {
        sharedPreferences.edit().putInt(GET_SKU_DETAILS, response).apply();
    }

    public int getConsumePurchaseResponse() {
        return sharedPreferences.getInt(CONSUME_PURCHASE, RESULT_DEFAULT);
    }

    void setConsumePurchaseResponse(int response) {
        sharedPreferences.edit().putInt(CONSUME_PURCHASE, response).apply();
    }

    public int getGetBuyIntentToReplaceSkusResponse() {
        return sharedPreferences.getInt(GET_BUY_INTENT_TO_REPLACE_SKUS, RESULT_DEFAULT);
    }

    void setGetBuyIntentToReplaceSkusResponse(int response) {
        sharedPreferences.edit().putInt(GET_BUY_INTENT_TO_REPLACE_SKUS, response).apply();
    }

    public int getReplaceResponse() {
        return sharedPreferences.getInt(REPLACE, RESULT_DEFAULT);
    }

    void setReplaceReponse(int response) {
        sharedPreferences.edit().putInt(REPLACE, response).apply();
    }

    @NonNull
    String getUsersResponse() {
        return sharedPreferences.getString(USERS, DEFAULT_USER);
    }

    void setUsersReponse(@NonNull String user) {
        sharedPreferences.edit().putString(USERS, user).apply();
    }
}
