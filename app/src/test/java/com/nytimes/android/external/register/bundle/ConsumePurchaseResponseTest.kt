package com.nytimes.android.external.register.bundle

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.collect.ImmutableList
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.BillingServiceStubImpl
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.PurchasesLists
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyListOf
import org.mockito.Matchers.anyString
import java.util.*

@RunWith(AndroidJUnit4::class)
class ConsumePurchaseResponseTest {

    private val apiOverrides: APIOverrides = mock()

    private val purchases: Purchases = mock()

    private val inAppPurchasesLists: PurchasesLists = mock()

    private val subscriptionsPurchasesLists: PurchasesLists = mock()

    private val buyIntentBundleBuilder: BuyIntentBundleBuilder = mock()

    private val skuDetailsBundleBuilder: SkuDetailsBundleBuilder = mock()

    private val purchasesBundleBuilder: PurchasesBundleBuilder = mock()

    private val buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder = mock()

    private lateinit var testObject: BillingServiceStubImpl

    private val expected = Bundle()

    private val type = GoogleUtil.BILLING_TYPE_IAP

    @Before
    fun setUp() {
        whenever(skuDetailsBundleBuilder.newBuilder()).thenReturn(skuDetailsBundleBuilder)
        whenever(skuDetailsBundleBuilder.skus(anyListOf(String::class.java), anyString())).thenReturn(skuDetailsBundleBuilder)
        whenever(skuDetailsBundleBuilder.build()).thenReturn(expected)
        whenever(buyIntentBundleBuilder.newBuilder()).thenReturn(buyIntentBundleBuilder)
        whenever(buyIntentBundleBuilder.developerPayload(anyString())).thenReturn(buyIntentBundleBuilder)
        whenever(buyIntentBundleBuilder.packageName(anyString())).thenReturn(buyIntentBundleBuilder)
        whenever(buyIntentBundleBuilder.sku(anyString())).thenReturn(buyIntentBundleBuilder)
        whenever(buyIntentBundleBuilder.type(anyString())).thenReturn(buyIntentBundleBuilder)
        whenever(buyIntentBundleBuilder.build()).thenReturn(expected)
        whenever(purchasesBundleBuilder.newBuilder()).thenReturn(purchasesBundleBuilder)
        whenever(purchasesBundleBuilder.type(anyString())).thenReturn(purchasesBundleBuilder)
        whenever(purchasesBundleBuilder.continuationToken(anyString())).thenReturn(purchasesBundleBuilder)
        whenever(purchasesBundleBuilder.build()).thenReturn(expected)
        val consumePurchaseResponse = ConsumePurchaseResponse(apiOverrides, purchases)
        testObject = BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, consumePurchaseResponse, buyIntentToReplaceSkusBundleBuilder)
    }

    @Test
    fun testConsumePurchaseIAP() {
        val testPurchases = mutableListOf("purchase1", "purchase2")
        val subscriptions = mutableListOf("subscription1", "subscription2")
        val getPurchasesBundle = Bundle()
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, ArrayList(testPurchases))
        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null)).thenReturn(inAppPurchasesLists)
        whenever(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        whenever(purchases.removePurchase("purchase1", GoogleUtil.BILLING_TYPE_IAP)).thenReturn(true)

        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        whenever(subscriptionsPurchasesLists.purchaseDataList).thenReturn(subscriptions)

        whenever(purchasesBundleBuilder.build()).thenReturn(getPurchasesBundle)

        val stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token")
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases)
        assertThat(stored).isEqualTo(getPurchasesBundle)

        val purchaseToken = testPurchases[0]
        val result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken)
        assertThat(result).isEqualTo(GoogleUtil.RESULT_OK)
    }

    @Test
    fun testConsumePurchaseSubscription() {
        val testPurchases = ImmutableList.of("purchase1", "purchase2")
        val subscriptions = ImmutableList.of("subscription1", "subscription2")
        val getPurchasesBundle = Bundle()
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, ArrayList(testPurchases))
        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists)
        whenever(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        whenever(subscriptionsPurchasesLists.purchaseDataList)
                .thenReturn(subscriptions)
        whenever(purchasesBundleBuilder.build())
                .thenReturn(getPurchasesBundle)

        val stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token")
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases)
        assertThat(stored).isEqualTo(getPurchasesBundle)

        val purchaseToken = subscriptions[0]

        val result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken)
        assertThat(result).isEqualTo(GoogleUtil.RESULT_ERROR)
    }

    @Test
    fun testConsumePurchaseNotOwned() {
        val testPurchases = ImmutableList.of("purchase1", "purchase2")
        val subscriptions = ImmutableList.of("subscription1", "subscription2")
        val getPurchasesBundle = Bundle()
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, ArrayList(testPurchases))
        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists)
        whenever(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        whenever(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        whenever(subscriptionsPurchasesLists.purchaseDataList)
                .thenReturn(subscriptions)
        whenever(purchasesBundleBuilder.build())
                .thenReturn(getPurchasesBundle)

        val stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token")
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases)
        assertThat(stored).isEqualTo(getPurchasesBundle)

        val purchaseToken = "Not owned product"

        val result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken)
        assertThat(result).isEqualTo(GoogleUtil.RESULT_ITEM_NOT_OWNED)
    }

    companion object {
        private const val API_VERSION = 3
        private const val PACKAGE_NAME = "com.my.package"
    }

}
