package com.nytimes.android.external.registerlib

import android.app.Activity
import android.content.Context
import androidx.annotation.UiThread
import com.android.billingclient.api.*

/**
 * Interface above real test Google Service (in app billing)
 */
abstract class GoogleServiceProvider {

    /**
     * Checks if the client is currently connected to the service, so that requests to other methods
     * will succeed.
     *
     *
     * Returns true if the client is currently connected to the service, false otherwise.
     *
     *
     * Note: It also means that INAPP items are supported for purchasing, queries and all other
     * actions. If you need to check support for SUBSCRIPTIONS or something different, use [ ][.isFeatureSupported] method.
     */
    @UiThread
    abstract fun isReady(): Boolean

    /** Builder to configure and create a BillingClient instance.  */
    class Builder constructor(private val mContext: Context?) {
        private var mListener: PurchasesUpdatedListener? = null
        private var useTestProvider: Boolean = false
        private var underAgeOfConsent: Int = BillingClient.UnderAgeOfConsent.UNSPECIFIED
        private var childDirected: Int = BillingClient.ChildDirected.UNSPECIFIED
        private var enablePendingPurchases: Boolean = false

        /**
         * Specify a valid listener for onPurchasesUpdated event.
         *
         * @param listener Your listener for app initiated and Play Store initiated purchases.
         */
        @UiThread
        fun setListener(listener: PurchasesUpdatedListener): GoogleServiceProvider.Builder {
            mListener = listener
            return this
        }

        @UiThread
        fun useTestProvider(useTestProvider: Boolean): GoogleServiceProvider.Builder {
            this.useTestProvider = useTestProvider
            return this
        }

        @UiThread
        fun setUnderAgeOfConsent(underAgeOfConsent: Int): GoogleServiceProvider.Builder {
            this.underAgeOfConsent = underAgeOfConsent
            return this
        }

        @UiThread
        fun setChildDirected(childDirected: Int): GoogleServiceProvider.Builder {
            this.childDirected = childDirected
            return this
        }

        @UiThread
        fun enablePendingPurchases(): GoogleServiceProvider.Builder {
            this.enablePendingPurchases = true
            return this
        }

        /**
         * Creates a Billing client instance.
         *
         *
         * After creation, it will not yet be ready to use. You must initiate setup by calling [ ][.startConnection] and wait for setup to complete.
         *
         * @return BillingClient instance
         * @throws IllegalArgumentException if Context or PurchasesUpdatedListener were not set.
         */
        @UiThread
        fun build(): GoogleServiceProvider {
            if (mContext == null) {
                throw IllegalArgumentException("Please provide a valid Context.")
            }
            if (mListener == null) {
                throw IllegalArgumentException(
                        "Please provide a valid listener for" + " purchases updates.")
            }

            return if (useTestProvider) {
                GoogleServiceProviderTesting(mContext,childDirected, underAgeOfConsent, enablePendingPurchases, mListener!!)
            } else {
                GoogleServiceProviderImpl(mContext,childDirected, underAgeOfConsent, enablePendingPurchases, mListener!!)
            }
        }
    }

    /**
     * Check if specified feature or capability is supported by the Play Store.
     *
     * @param feature One of [BillingClient.FeatureType] constants.
     * @return BILLING_RESULT_OK if feature is supported and corresponding error code otherwise.
     */
    @UiThread
    abstract fun isFeatureSupported(@BillingClient.FeatureType feature: String): BillingResult

    /**
     * Starts up BillingClient setup process asynchronously. You will be notified through the [ ] listener when the setup process is complete.
     *
     * @param listener The listener to notify when the setup process is complete.
     */
    @UiThread
    abstract fun startConnection(listener: BillingClientStateListener)

    /**
     * Close the connection and release all held resources such as service connections.
     *
     *
     * Call this method once you are done with this BillingClient reference.
     */
    @UiThread
    abstract fun endConnection()

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     *
     * It will show the Google Play purchase screen. The result will be delivered via the [ ] interface implementation reported to the TODO
     * constructor.
     *
     * @param activity An activity reference from which the billing flow will be launched.
     * @param params Params specific to the request [BillingFlowParams]).
     * @return BillingResult The response code ([BillingClient.BillingResponse]) of launch flow operation.
     */
    @UiThread
    abstract fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult

    /**
     * Get purchases details for all the items bought within your app. This method uses a cache of
     * Google Play Store app without initiating a network request.
     *
     *
     * Note: It's recommended for security purposes to go through purchases verification on your
     * backend (if you have one) by calling the following API:
     * https://developers.google.com/android-publisher/api-ref/purchases/products/get
     *
     * @param skuType The type of SKU, either "inapp" or "subs" as in [BillingClient.SkuType].
     * @return PurchasesResult The [Purchase.PurchasesResult] containing the list of purchases and the
     * response code ([BillingClient.BillingResponse]
     */
    @UiThread
    abstract fun queryPurchases(@BillingClient.SkuType skuType: String): Purchase.PurchasesResult

    /**
     * Perform a network query to get SKU details and return the result asynchronously.
     *
     * @param params Params specific to this query request [SkuDetailsParams].
     * @param listener Implement it to get the result of your query operation returned asynchronously
     * through the callback with the [BillingClient.BillingResponse] and the list of [SkuDetails].
     */
    @UiThread
    abstract fun querySkuDetailsAsync(
            params: SkuDetailsParams, listener: SkuDetailsResponseListener)

    /**
     * Consumes a given in-app product. Consuming can only be done on an item that's owned, and as a
     * result of consumption, the user will no longer own it.
     *
     *
     * Consumption is done asynchronously and the listener receives the callback specified upon
     * completion.
     *
     * @param purchaseToken The purchase token of the item to consume.
     * @param listener Implement it to get the result of your consume operation returned
     * asynchronously through the callback with token and [BillingClient.BillingResponse] parameters.
     */
    @UiThread
    abstract fun consumeAsync(params: ConsumeParams, listener: ConsumeResponseListener)

    /**
     * Returns the most recent purchase made by the user for each SKU, even if that purchase is
     * expired, canceled, or consumed.
     *
     * @param skuType The type of SKU, either "inapp" or "subs" as in [BillingClient.SkuType].
     * @param listener Implement it to get the result of your query returned asynchronously through
     * the callback with a [Purchase.PurchasesResult] parameter.
     */
    @UiThread
    abstract fun queryPurchaseHistoryAsync(
            @BillingClient.SkuType skuType: String, listener: PurchaseHistoryResponseListener)

    companion object {

        /**
         * Constructs a new [BillingClient.Builder] instance.
         *
         * @param context It will be used to get an application context to bind to the in-app billing
         * service.
         */
        @UiThread
        @JvmStatic
        fun newBuilder(context: Context): GoogleServiceProvider.Builder {
            return GoogleServiceProvider.Builder(context)
        }
    }
}
