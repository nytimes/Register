package com.nytimes.android.external.register.bundle

import com.google.common.collect.ImmutableList
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.PurchasesLists
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PurchasesBundleBuilderTest {

    private lateinit var testObject: PurchasesBundleBuilder

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Mock
    private lateinit var purchases: Purchases

    @Mock
    private lateinit var purchasesLists: PurchasesLists

    private val type = GoogleUtil.BILLING_TYPE_SUBSCRIPTION
    private val purchasesDataList = ImmutableList.of("purchase1Data", "purchase2Data")
    private val purchaseItemList = ImmutableList.of("item1", "item2")
    private val signedPurchaseList = ImmutableList.of("signed1", "signed2")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testObject = PurchasesBundleBuilder(apiOverrides, purchases)
    }

    @Test
    fun testBundleOKNoContinuationToken() {
        `when`(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_OK)
        `when`(purchases.getPurchasesLists(type, null)).thenReturn(purchasesLists)
        `when`(purchasesLists.purchaseDataList).thenReturn(purchasesDataList)
        `when`(purchasesLists.purchaseItemList).thenReturn(purchaseItemList)
        `when`(purchasesLists.dataSignatureList).thenReturn(signedPurchaseList)
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
        `when`(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_OK)
        `when`(purchases.getPurchasesLists(type, continuationToken)).thenReturn(purchasesLists)
        `when`(purchasesLists.purchaseDataList).thenReturn(purchasesDataList)
        `when`<String>(purchasesLists.continuationToken).thenReturn(continuationToken)
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
        `when`(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_ERROR)

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
        verify<APIOverrides>(apiOverrides).getPurchasesResponse
    }
}

