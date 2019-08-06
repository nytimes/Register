package com.nytimes.android.external.register

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.collect.ImmutableSet
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides.Companion.PREF_NAME
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.InAppPurchaseData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyString
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class PurchasesTest {
    
    private val signer: Signer = mock()

    private lateinit var testObject: Purchases
    private lateinit var inAppPurchaseData1: InAppPurchaseData
    private lateinit var inAppPurchaseData2: InAppPurchaseData
    private lateinit var inAppPurchaseData1Str: String
    private lateinit var inAppPurchaseData2Str: String
    private var continuationToken: String? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val sharedPreferences = RuntimeEnvironment.application
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        inAppPurchaseData1 = InAppPurchaseData.Builder()
                .orderId(ORDER_ID_1)
                .packageName(PACKAGE_1)
                .productId(PRODUCT_ID_1)
                .purchaseTime(PURCHASE_TIME_1)
                .purchaseState(PURCHASE_STATE_1)
                .developerPayload(DEV_PAYLOAD_1)
                .purchaseToken(PURCHASE_TOKEN_1)
                .build()

        inAppPurchaseData2 = InAppPurchaseData.Builder()
                .orderId(ORDER_ID_2)
                .packageName(PACKAGE_2)
                .productId(PRODUCT_ID_2)
                .purchaseTime(PURCHASE_TIME_2)
                .purchaseState(PURCHASE_STATE_2)
                .developerPayload(DEV_PAYLOAD_2)
                .purchaseToken(PURCHASE_TOKEN_2)
                .build()

        inAppPurchaseData1Str = InAppPurchaseData.toJson(inAppPurchaseData1)
        inAppPurchaseData2Str = InAppPurchaseData.toJson(inAppPurchaseData2)

        testObject = Purchases(sharedPreferences, signer)

        whenever(signer.signData(anyString())).thenReturn("signedData")
    }

    @Test
    fun testAddPurchaseAndGetInAppPurchaseData() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP)

        val iapdActual1 = testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        val iapdActual2 = testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP)

        assertThat(iapdActual1.iterator().next()).isEqualTo(inAppPurchaseData1)
        assertThat(iapdActual2.iterator().next()).isEqualTo(inAppPurchaseData2)
    }

    @Test
    fun testGetInAppPurchaseDataAsArrayList() {
        continuationToken = ""
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP)

        val purchasesSub = testObject.getPurchasesLists(
                GoogleUtil.BILLING_TYPE_SUBSCRIPTION, continuationToken)
        val purchasesIAP = testObject.getPurchasesLists(
                GoogleUtil.BILLING_TYPE_IAP, continuationToken)
        val subList = purchasesSub.purchaseDataList
        val iapList = purchasesIAP.purchaseDataList

        assertThat(subList[0])
                .isEqualTo(inAppPurchaseData1Str)
        assertThat(iapList[0])
                .isEqualTo(inAppPurchaseData2Str)
        assertThat(purchasesSub.continuationToken).isNull()
        assertThat(purchasesIAP.continuationToken).isNull()
    }

    @Test
    fun testGetInAppPurchaseDataAsArrayListWithContinuationToken() {
        // add PAGE_LIMIT + OVER+LIMIT purchases
        val overLimit = 3
        for (i in 0 until Purchases.PAGE_LIMIT + overLimit) {
            val inAppPurchaseData = InAppPurchaseData.Builder()
                    .orderId(ORDER_ID_1 + i)
                    .packageName(PACKAGE_1)
                    .productId(PRODUCT_ID_1)
                    .purchaseTime(PURCHASE_TIME_1)
                    .purchaseState(PURCHASE_STATE_1)
                    .developerPayload(DEV_PAYLOAD_1)
                    .purchaseToken(PURCHASE_TOKEN_1)
                    .build()
            val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
            testObject.addPurchase(inAppPurchaseDataStr, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        }

        // get first page
        var purchasesSub: PurchasesLists = testObject.getPurchasesLists(
                GoogleUtil.BILLING_TYPE_SUBSCRIPTION, continuationToken)

        // check
        var subList = purchasesSub.purchaseDataList
        assertThat(subList.size).isEqualTo(Purchases.PAGE_LIMIT)
        assertThat(purchasesSub.continuationToken).isEqualTo(Integer.toString(Purchases.PAGE_LIMIT))

        // get second page
        purchasesSub = testObject.getPurchasesLists(
                GoogleUtil.BILLING_TYPE_SUBSCRIPTION, purchasesSub.continuationToken)

        // check
        subList = purchasesSub.purchaseDataList
        assertThat(subList.size).isEqualTo(overLimit)
        assertThat(purchasesSub.continuationToken).isNull()
    }

    @Test
    fun testGetReceiptForSkuFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP)

        val actual = testObject.getReceiptsForSkus(ImmutableSet.of(PRODUCT_ID_1),
                GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        assertThat(actual)
                .isEqualTo(ImmutableSet.of(PURCHASE_TOKEN_1))
    }

    @Test
    fun testGetReceiptForSkuNotFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP)

        val actual = testObject.getReceiptsForSkus(ImmutableSet.of(PRODUCT_ID_1),
                GoogleUtil.BILLING_TYPE_IAP)
        assertThat(actual)
                .isEqualTo(ImmutableSet.of<Any>())
    }

    @Test
    fun testPurgePurchases() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP)
        testObject.purgePurchases()
        assertThat(testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION).size)
                .isEqualTo(0)
        assertThat(testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP).size)
                .isEqualTo(0)
    }

    companion object {
        private val ORDER_ID_1 = "order1"
        private val ORDER_ID_2 = "order2"
        private val PACKAGE_1 = "package1"
        private val PACKAGE_2 = "package2"
        private val PRODUCT_ID_1 = "product1"
        private val PRODUCT_ID_2 = "product2"
        private val PURCHASE_TIME_1 = "purchaseTime1"
        private val PURCHASE_TIME_2 = "purchaseTime2"
        private val PURCHASE_STATE_1 = "purchaseState1"
        private val PURCHASE_STATE_2 = "purchaseState2"
        private val DEV_PAYLOAD_1 = "devPayload1"
        private val DEV_PAYLOAD_2 = "devPayload2"
        private val PURCHASE_TOKEN_1 = "purchaseToken1"
        private val PURCHASE_TOKEN_2 = "purchaseToken2"
    }
}
