package com.nytimes.android.external.playbillingtester;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

public class APIOverridesAndPurchases {
    public static final String PREF_NAME = "PlayInApBillingModel";
    public static final String CONFIG_FILE = "playbillingtester.json";
    public static final int RESULT_DEFAULT = -1;            // - no user override
    private static final String GOOGLE_PURCHASE_ITEMS_SUB = "GoogleItems";
    private static final String GOOGLE_PURCHASE_ITEMS_IAP = "GoogleItemsIAP";
    private static final String IS_BILLING_SUPPORTED = "isBillingSupported";
    private static final String GET_BUY_INTENT = "getBuyIntent";
    private static final String BUY = "Buy";
    private static final String GET_PURCHASES = "getPurchases";
    private static final String GET_SKU_DETAILS = "getSkuDetails";
    private static final String USERS = "Users";
    private static final String DEFAULT_USER = "200nyttest1@nytimes.com";

    @NonNull
    protected Gson gson;

    @NonNull
    private final SharedPreferences sharedPreferences;

    @Inject
    public APIOverridesAndPurchases(@NonNull SharedPreferences sharedPreferences, @NonNull Gson gson) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
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

    @NonNull
    String getUsersResponse() {
        return sharedPreferences.getString(USERS, DEFAULT_USER);
    }

    void setUsersReponse(@NonNull String user) {
        sharedPreferences.edit().putString(USERS, user).apply();
    }

    void addPurchase(@NonNull String inAppPurchaseDataStr, @NonNull String itemType) {
        Set<String> items = sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<String>());
        items.add(inAppPurchaseDataStr);
        sharedPreferences.edit().putStringSet(getItemsKeyFromType(itemType), items).apply();
    }

    @NonNull
    Set<InAppPurchaseData> getInAppPurchaseData(@NonNull String itemType) {
        Set<InAppPurchaseData> ret = new LinkedHashSet<>();
        for (String json : sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<String>())) {
            ret.add(gson.fromJson(json, InAppPurchaseData.class));
        }
        return ret;
    }

    @NonNull
    public List<String> getInAppPurchaseDataAsArrayList(@NonNull String type) {
        ArrayList<String> ret = new ArrayList<>();
        for (InAppPurchaseData inAppPurchaseData: getInAppPurchaseData(type)) {
            ret.add(gson.toJson(inAppPurchaseData, InAppPurchaseData.class));
        }
        return ret;
    }

    @NonNull
    Optional<String> getReceiptForSku(@NonNull String sku, @NonNull String itemType) {
        Optional<String> receipt = Optional.absent();
        for (String json : sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<String>())) {
            InAppPurchaseData inAppPurchaseData = gson.fromJson(json, InAppPurchaseData.class);
            if (inAppPurchaseData.productId().equals(sku)) {
                receipt = Optional.of(inAppPurchaseData.purchaseToken());
            }
        }
        return receipt;
    }

    void purgePurchases() {
        sharedPreferences.edit().remove(GOOGLE_PURCHASE_ITEMS_IAP).remove(GOOGLE_PURCHASE_ITEMS_SUB).apply();
    }

    @NonNull
    private String getItemsKeyFromType(@NonNull String type) {
        if (GoogleUtil.BILLING_TYPE_IAP.equals(type)) {
            return GOOGLE_PURCHASE_ITEMS_IAP;
        } else {
            return GOOGLE_PURCHASE_ITEMS_SUB;
        }
    }
}
