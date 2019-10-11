package com.nytimes.android.external.register

import android.os.Bundle
import android.os.RemoteException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides.Companion.RESULT_DEFAULT
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyListOf
import org.mockito.Matchers.anyString
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.util.*

@RunWith(AndroidJUnit4::class)
class BillingServicesSubImplTest {

    private val apiOverrides: APIOverrides = mock()

    private val buyIntentBundleBuilder: BuyIntentBundleBuilder = mock()

    private val skuDetailsBundleBuilder: SkuDetailsBundleBuilder = mock()

    private val purchasesBundleBuilder: PurchasesBundleBuilder = mock()

    private val buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder = mock()

    private lateinit var testObject: BillingServiceStubImpl

    private val type = GoogleUtil.BILLING_TYPE_IAP
    private val expected = Bundle()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

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
        whenever(buyIntentToReplaceSkusBundleBuilder.newBuilder()).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.developerPayload(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.packageName(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.newSku(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.oldSkus(anyListOf(String::class.java)))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.type(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        whenever(buyIntentToReplaceSkusBundleBuilder.build()).thenReturn(expected)

        testObject = BillingServiceStubImpl(apiOverrides,
                buyIntentBundleBuilder,
                skuDetailsBundleBuilder,
                purchasesBundleBuilder,
                mock(ConsumePurchaseResponse::class.java),
                buyIntentToReplaceSkusBundleBuilder)
    }

    @Test
    fun testIsBillingSupportedDefault() {
        whenever(apiOverrides.isBillingSupportedResponse).thenReturn(RESULT_DEFAULT)

        val actual = testObject.isBillingSupported(API_VERSION, PACKAGE_NAME, type)

        assertThat(actual).isEqualTo(GoogleUtil.RESULT_OK)
    }

    @Test
    fun testIsBillingSupportedNonDefault() {
        val expected = GoogleUtil.RESULT_BILLING_UNAVAILABLE
        whenever(apiOverrides.isBillingSupportedResponse).thenReturn(expected)

        val actual = testObject.isBillingSupported(API_VERSION, PACKAGE_NAME, type)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testGetSkuDetails() {
        val skusBundle = Bundle()
        skusBundle.putStringArrayList(GoogleUtil.ITEM_ID_LIST, ArrayList())

        val actual = testObject.getSkuDetails(API_VERSION, PACKAGE_NAME, type, skusBundle)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testGetBuyIntent() {
        val actual = testObject.getBuyIntent(API_VERSION, SKU, PACKAGE_NAME, type, DEVELOPER_PAYLOAD)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testGetPurchases() {
        val actual = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "conti")

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testConsumePurchase() {
        val actual = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, "token")

        assertThat(actual).isEqualTo(GoogleUtil.RESULT_OK)
    }

    @Test
    @Throws(RemoteException::class)
    fun testStub() {
        val actual = testObject.stub(API_VERSION, PACKAGE_NAME, type)

        assertThat(actual).isEqualTo(0)
    }

    @Test
    @Throws(RemoteException::class)
    fun testGetBuyIntentToReplaceSkus() {
        val actual = testObject.getBuyIntentToReplaceSkus(API_VERSION, PACKAGE_NAME,
                listOf(SKU), "sku2", type, DEVELOPER_PAYLOAD)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testAsBinder() {
        assertThat(testObject.asBinder()).isEqualTo(testObject)
    }

    companion object {
        private const val API_VERSION = 3
        private const val PACKAGE_NAME = "com.my.package"
        private const val DEVELOPER_PAYLOAD = "devPayload"
        private const val SKU = "sku"
    }
}
