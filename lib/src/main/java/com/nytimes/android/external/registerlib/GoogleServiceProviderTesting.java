package com.nytimes.android.external.registerlib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingClientTesting;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

public class GoogleServiceProviderTesting extends GoogleServiceProvider {

    private final BillingClientTesting billingClient;

    GoogleServiceProviderTesting(Context context, PurchasesUpdatedListener listener) {
        billingClient = new BillingClientTesting(context, listener);
    }

    @Override
    public int isFeatureSupported(String feature) {
        return billingClient.isFeatureSupported(feature);
    }

    @Override
    public boolean isReady() {
        return billingClient.isReady();
    }

    @Override
    public void startConnection(@NonNull BillingClientStateListener listener) {
        billingClient.startConnection(listener);
    }

    @Override
    public void endConnection() {
        billingClient.endConnection();
    }

    @Override
    public int launchBillingFlow(Activity activity, BillingFlowParams params) {
        return billingClient.launchBillingFlow(activity, params);
    }

    @Override
    public Purchase.PurchasesResult queryPurchases(String skuType) {
        return billingClient.queryPurchases(skuType);
    }

    @Override
    public void querySkuDetailsAsync(SkuDetailsParams params, SkuDetailsResponseListener listener) {
        billingClient.querySkuDetailsAsync(params, listener);
    }

    @Override
    public void consumeAsync(String purchaseToken, ConsumeResponseListener listener) {
        billingClient.consumeAsync(purchaseToken, listener);
    }

    @Override
    public void queryPurchaseHistoryAsync(String skuType, PurchaseHistoryResponseListener listener) {
        billingClient.queryPurchaseHistoryAsync(skuType, listener);
    }
}
