package com.nytimes.android.external.register.bundle

import android.os.Bundle
import com.google.common.collect.ImmutableList
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
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ConsumePurchaseResponseTest {

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Mock
    private lateinit var purchases: Purchases

    @Mock
    private lateinit var inAppPurchasesLists: PurchasesLists

    @Mock
    private lateinit var subscriptionsPurchasesLists: PurchasesLists

    @Mock
    private lateinit var buyIntentBundleBuilder: BuyIntentBundleBuilder

    @Mock
    private lateinit var skuDetailsBundleBuilder: SkuDetailsBundleBuilder

    @Mock
    private lateinit var purchasesBundleBuilder: PurchasesBundleBuilder

    @Mock
    private lateinit var buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder

    private lateinit var testObject: BillingServiceStubImpl

    private val expected = Bundle()

    private val type = GoogleUtil.BILLING_TYPE_IAP

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        `when`(skuDetailsBundleBuilder.newBuilder()).thenReturn(skuDetailsBundleBuilder)
        `when`(skuDetailsBundleBuilder.skus(anyListOf(String::class.java), anyString())).thenReturn(skuDetailsBundleBuilder)
        `when`(skuDetailsBundleBuilder.build()).thenReturn(expected)
        `when`(buyIntentBundleBuilder.newBuilder()).thenReturn(buyIntentBundleBuilder)
        `when`(buyIntentBundleBuilder.developerPayload(anyString())).thenReturn(buyIntentBundleBuilder)
        `when`(buyIntentBundleBuilder.packageName(anyString())).thenReturn(buyIntentBundleBuilder)
        `when`(buyIntentBundleBuilder.sku(anyString())).thenReturn(buyIntentBundleBuilder)
        `when`(buyIntentBundleBuilder.type(anyString())).thenReturn(buyIntentBundleBuilder)
        `when`(buyIntentBundleBuilder.build()).thenReturn(expected)
        `when`(purchasesBundleBuilder.newBuilder()).thenReturn(purchasesBundleBuilder)
        `when`(purchasesBundleBuilder.type(anyString())).thenReturn(purchasesBundleBuilder)
        `when`(purchasesBundleBuilder.continuationToken(anyString())).thenReturn(purchasesBundleBuilder)
        `when`(purchasesBundleBuilder.build()).thenReturn(expected)
        val consumePurchaseResponse = ConsumePurchaseResponse(apiOverrides, purchases)
        testObject = BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, consumePurchaseResponse, buyIntentToReplaceSkusBundleBuilder)
    }

    @Test
    fun testConsumePurchaseIAP() {
        val testPurchases = ImmutableList.of("purchase1", "purchase2")
        val subscriptions = ImmutableList.of("subscription1", "subscription2")
        val getPurchasesBundle = Bundle()
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, ArrayList(testPurchases))
        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null)).thenReturn(inAppPurchasesLists)
        `when`(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        `when`(purchases.removePurchase("purchase1", GoogleUtil.BILLING_TYPE_IAP)).thenReturn(true)

        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        `when`(subscriptionsPurchasesLists.purchaseDataList).thenReturn(subscriptions)

        `when`(purchasesBundleBuilder.build()).thenReturn(getPurchasesBundle)

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
        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists)
        `when`(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        `when`(subscriptionsPurchasesLists.purchaseDataList)
                .thenReturn(subscriptions)
        `when`(purchasesBundleBuilder.build())
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
        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists)
        `when`(inAppPurchasesLists.purchaseDataList).thenReturn(testPurchases)
        `when`(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists)
        `when`(subscriptionsPurchasesLists.purchaseDataList)
                .thenReturn(subscriptions)
        `when`(purchasesBundleBuilder.build())
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
