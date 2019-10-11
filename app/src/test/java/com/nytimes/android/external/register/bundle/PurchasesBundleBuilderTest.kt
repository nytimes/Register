package com.nytimes.android.external.register.bundle

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.PurchasesLists
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PurchasesBundleBuilderTest {

    private lateinit var testObject: PurchasesBundleBuilder

    private val apiOverrides: APIOverrides = mock()

    private val purchases: Purchases = mock()

    private val purchasesLists: PurchasesLists = mock()

    private val type = GoogleUtil.BILLING_TYPE_SUBSCRIPTION
    private val purchasesDataList = mutableListOf("purchase1Data", "purchase2Data")
    private val purchaseItemList = mutableListOf("item1", "item2")
    private val signedPurchaseList = mutableListOf("signed1", "signed2")

    @Before
    fun setUp() {
        testObject = PurchasesBundleBuilder(apiOverrides, purchases)
    }

    @Test
    fun testBundleOKNoContinuationToken() {
        whenever(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_OK)
        whenever(purchases.getPurchasesLists(type, null)).thenReturn(purchasesLists)
        whenever(purchasesLists.purchaseDataList).thenReturn(purchasesDataList)
        whenever(purchasesLists.purchaseItemList).thenReturn(purchaseItemList)
        whenever(purchasesLists.dataSignatureList).thenReturn(signedPurchaseList)
        val bundle = testObject.newBuilder()
                .type(type)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(purchasesDataList)
        assertThat(bundle.getString(GoogleUtil.INAPP_CONTINUATION_TOKEN)).isNull()
    }

    @Test
    fun testBundleOKWithContinuationToken() {
        val continuationToken = "100"
        whenever(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_OK)
        whenever(purchases.getPurchasesLists(type, continuationToken)).thenReturn(purchasesLists)
        whenever(purchasesLists.purchaseDataList).thenReturn(purchasesDataList)
        whenever(purchasesLists.continuationToken).thenReturn(continuationToken)
        val bundle = testObject.newBuilder()
                .type(type)
                .continuationToken(continuationToken)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(purchasesDataList)
        assertThat(bundle.getString(GoogleUtil.INAPP_CONTINUATION_TOKEN)).isEqualTo(continuationToken)
    }

    @Test
    fun testBundleNotOK() {
        whenever(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_ERROR)

        val bundle = testObject.newBuilder()
                .type(type)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR)
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isNull()
    }

    @Test
    fun testRawResponseCode() {
        testObject.rawResponseCode()
        verify(apiOverrides).getPurchasesResponse
    }
}

