package com.nytimes.android.external.register

import androidx.annotation.IdRes

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIOverridesDelegate @Inject constructor(private val apiOverrides: APIOverrides) {

    fun setApiOverridesValue(@IdRes containerLayoutId: Int, item: ConfigResponse) {
        when (containerLayoutId) {
            R.id.isBillingSupported -> apiOverrides.isBillingSupportedResponse = item.responseCode
            R.id.getBuyIntent -> apiOverrides.getBuyIntentResponse = item.responseCode
            R.id.buy -> apiOverrides.buyResponse = item.responseCode
            R.id.getPurchases -> apiOverrides.getPurchasesResponse = item.responseCode
            R.id.getSkuDetails -> apiOverrides.getSkuDetailsResponse = item.responseCode
            R.id.consumePurchase -> apiOverrides.consumePurchaseResponse = item.responseCode
            R.id.getBuyIntentToReplaceSkus -> apiOverrides.getBuyIntentToReplaceSkusResponse = item.responseCode
            else -> {
                // No Op - Unknown id
            }
        }
    }

    fun getApiOverridesValue(@IdRes containerLayoutId: Int): Int {
        return when (containerLayoutId) {
            R.id.isBillingSupported -> apiOverrides.isBillingSupportedResponse
            R.id.getBuyIntent -> apiOverrides.getBuyIntentResponse
            R.id.buy -> apiOverrides.buyResponse
            R.id.getPurchases -> apiOverrides.getPurchasesResponse
            R.id.getSkuDetails -> apiOverrides.getSkuDetailsResponse
            R.id.consumePurchase -> apiOverrides.consumePurchaseResponse
            R.id.getBuyIntentToReplaceSkus -> apiOverrides.getBuyIntentToReplaceSkusResponse
            else -> -1
        }
    }
}
