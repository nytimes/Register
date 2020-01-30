package com.nytimes.android.external.registerlib

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class GoogleServiceProviderImpl(
        context: Context,
        childDirected: Int,
        underAgeOfConsent: Int,
        enablePendingPurchases: Boolean,
        listener: PurchasesUpdatedListener
) : GoogleServiceProvider() {

    private val billingClient: BillingClient by lazy {
        val builder = BillingClient.newBuilder(context)
                .setListener(listener)
                .setChildDirected(childDirected)
                .setUnderAgeOfConsent(underAgeOfConsent)

        if (enablePendingPurchases) {
            builder.enablePendingPurchases()
        }

        builder.build()
    }

    override fun isReady(): Boolean = billingClient.isReady

    override fun isFeatureSupported(feature: String): BillingResult {
        return billingClient.isFeatureSupported(feature)
    }

    override fun startConnection(listener: BillingClientStateListener) {
        billingClient.startConnection(listener)
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        return billingClient.launchBillingFlow(activity, params)
    }

    override fun queryPurchases(skuType: String): Purchase.PurchasesResult {
        return billingClient.queryPurchases(skuType)
    }

    override fun querySkuDetailsAsync(params: SkuDetailsParams, listener: SkuDetailsResponseListener) {
        billingClient.querySkuDetailsAsync(params, listener)
    }

    override fun consumeAsync(params: ConsumeParams, listener: ConsumeResponseListener) {
        billingClient.consumeAsync(params, listener)
    }

    override fun queryPurchaseHistoryAsync(skuType: String, listener: PurchaseHistoryResponseListener) {
        billingClient.queryPurchaseHistoryAsync(skuType, listener)
    }
}
