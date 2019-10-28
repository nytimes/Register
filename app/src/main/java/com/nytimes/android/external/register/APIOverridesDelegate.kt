package com.nytimes.android.external.register

import androidx.annotation.IdRes

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class APIOverridesDelegate @Inject constructor(private val apiOverrides: APIOverrides) {

    fun setApiOverridesValue(@IdRes containerLayoutId: Int, item: ConfigResponse) {
        when (containerLayoutId) {
            R.id.nyt_register_isBillingSupported -> apiOverrides.isBillingSupportedResponse = item.responseCode
            R.id.nyt_register_getBuyIntent -> apiOverrides.getBuyIntentResponse = item.responseCode
            R.id.nyt_register_buy -> apiOverrides.buyResponse = item.responseCode
            R.id.nyt_register_getPurchases -> apiOverrides.getPurchasesResponse = item.responseCode
            R.id.nyt_register_getSkuDetails -> apiOverrides.getSkuDetailsResponse = item.responseCode
            R.id.nyt_register_consumePurchase -> apiOverrides.consumePurchaseResponse = item.responseCode
            R.id.nyt_register_getBuyIntentToReplaceSkus -> apiOverrides.getBuyIntentToReplaceSkusResponse = item.responseCode
            else -> {
                // No Op - Unknown id
            }
        }
    }

    fun getApiOverridesValue(@IdRes containerLayoutId: Int): Int {
        return when (containerLayoutId) {
            R.id.nyt_register_isBillingSupported -> apiOverrides.isBillingSupportedResponse
            R.id.nyt_register_getBuyIntent -> apiOverrides.getBuyIntentResponse
            R.id.nyt_register_buy -> apiOverrides.buyResponse
            R.id.nyt_register_getPurchases -> apiOverrides.getPurchasesResponse
            R.id.nyt_register_getSkuDetails -> apiOverrides.getSkuDetailsResponse
            R.id.nyt_register_consumePurchase -> apiOverrides.consumePurchaseResponse
            R.id.nyt_register_getBuyIntentToReplaceSkus -> apiOverrides.getBuyIntentToReplaceSkusResponse
            else -> -1
        }
    }
}
