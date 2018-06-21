package com.nytimes.android.external.registerlib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

/**
 * Interface above real test Google Service (in app billing)
 */
public abstract class GoogleServiceProvider {

    /** Builder to configure and create a BillingClient instance. */
    public static final class Builder {
        private final Context mContext;
        private PurchasesUpdatedListener mListener;
        private boolean useTestProvider;

        private Builder(Context context) {
            mContext = context;
        }

        /**
         * Specify a valid listener for onPurchasesUpdated event.
         *
         * @param listener Your listener for app initiated and Play Store initiated purchases.
         */
        @UiThread
        public GoogleServiceProvider.Builder setListener(PurchasesUpdatedListener listener) {
            mListener = listener;
            return this;
        }

        @UiThread
        public GoogleServiceProvider.Builder useTestProvider(boolean useTestProvider) {
            this.useTestProvider = useTestProvider;
            return this;
        }

        /**
         * Creates a Billing client instance.
         *
         * <p>After creation, it will not yet be ready to use. You must initiate setup by calling {@link
         * #startConnection} and wait for setup to complete.
         *
         * @return BillingClient instance
         * @throws IllegalArgumentException if Context or PurchasesUpdatedListener were not set.
         */
        @UiThread
        public GoogleServiceProvider build() {
            if (mContext == null) {
                throw new IllegalArgumentException("Please provide a valid Context.");
            }
            if (mListener == null) {
                throw new IllegalArgumentException(
                        "Please provide a valid listener for" + " purchases updates.");
            }

            if (useTestProvider) {
                return new GoogleServiceProviderTesting(mContext, mListener);
            } else {
                return new GoogleServiceProviderImpl(mContext, mListener);
            }
        }
    }

    /**
     * Constructs a new {@link BillingClient.Builder} instance.
     *
     * @param context It will be used to get an application context to bind to the in-app billing
     *     service.
     */
    @UiThread
    public static GoogleServiceProvider.Builder newBuilder(@NonNull Context context) {
        return new GoogleServiceProvider.Builder(context);
    }

    /**
     * Check if specified feature or capability is supported by the Play Store.
     *
     * @param feature One of {@link BillingClient.FeatureType} constants.
     * @return BILLING_RESULT_OK if feature is supported and corresponding error code otherwise.
     */
    @UiThread
    public abstract @BillingClient.BillingResponse
    int isFeatureSupported(@BillingClient.FeatureType String feature);

    /**
     * Checks if the client is currently connected to the service, so that requests to other methods
     * will succeed.
     *
     * <p>Returns true if the client is currently connected to the service, false otherwise.
     *
     * <p>Note: It also means that INAPP items are supported for purchasing, queries and all other
     * actions. If you need to check support for SUBSCRIPTIONS or something different, use {@link
     * #isFeatureSupported(String)} method.
     */
    @UiThread
    public abstract boolean isReady();

    /**
     * Starts up BillingClient setup process asynchronously. You will be notified through the {@link
     * BillingClientStateListener} listener when the setup process is complete.
     *
     * @param listener The listener to notify when the setup process is complete.
     */
    @UiThread
    public abstract void startConnection(@NonNull final BillingClientStateListener listener);

    /**
     * Close the connection and release all held resources such as service connections.
     *
     * <p>Call this method once you are done with this BillingClient reference.
     */
    @UiThread
    public abstract void endConnection();

    /**
     * Initiate the billing flow for an in-app purchase or subscription.
     *
     * <p>It will show the Google Play purchase screen. The result will be delivered via the {@link
     * PurchasesUpdatedListener} interface implementation reported to the TODO
     * constructor.
     *
     * @param activity An activity reference from which the billing flow will be launched.
     * @param params Params specific to the request {@link BillingFlowParams}).
     * @return int The response code ({@link BillingClient.BillingResponse}) of launch flow operation.
     */
    @UiThread
    public abstract int launchBillingFlow(Activity activity, BillingFlowParams params);

    /**
     * Get purchases details for all the items bought within your app. This method uses a cache of
     * Google Play Store app without initiating a network request.
     *
     * <p>Note: It's recommended for security purposes to go through purchases verification on your
     * backend (if you have one) by calling the following API:
     * https://developers.google.com/android-publisher/api-ref/purchases/products/get
     *
     * @param skuType The type of SKU, either "inapp" or "subs" as in {@link BillingClient.SkuType}.
     * @return PurchasesResult The {@link Purchase.PurchasesResult} containing the list of purchases and the
     *     response code ({@link BillingClient.BillingResponse}
     */
    @UiThread
    public abstract Purchase.PurchasesResult queryPurchases(@BillingClient.SkuType String skuType);

    /**
     * Perform a network query to get SKU details and return the result asynchronously.
     *
     * @param params Params specific to this query request {@link SkuDetailsParams}.
     * @param listener Implement it to get the result of your query operation returned asynchronously
     *     through the callback with the {@link BillingClient.BillingResponse} and the list of {@link SkuDetails}.
     */
    @UiThread
    public abstract void querySkuDetailsAsync(
            SkuDetailsParams params, SkuDetailsResponseListener listener);

    /**
     * Consumes a given in-app product. Consuming can only be done on an item that's owned, and as a
     * result of consumption, the user will no longer own it.
     *
     * <p>Consumption is done asynchronously and the listener receives the callback specified upon
     * completion.
     *
     * @param purchaseToken The purchase token of the item to consume.
     * @param listener Implement it to get the result of your consume operation returned
     *     asynchronously through the callback with token and {@link BillingClient.BillingResponse} parameters.
     */
    @UiThread
    public abstract void consumeAsync(String purchaseToken, ConsumeResponseListener listener);

    /**
     * Returns the most recent purchase made by the user for each SKU, even if that purchase is
     * expired, canceled, or consumed.
     *
     * @param skuType The type of SKU, either "inapp" or "subs" as in {@link BillingClient.SkuType}.
     * @param listener Implement it to get the result of your query returned asynchronously through
     *     the callback with a {@link Purchase.PurchasesResult} parameter.
     */
    @UiThread
    public abstract void queryPurchaseHistoryAsync(
            @BillingClient.SkuType String skuType, PurchaseHistoryResponseListener listener);
}
