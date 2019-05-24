package com.nytimes.android.external.register

import android.os.Bundle
import android.os.RemoteException
import com.google.common.collect.ImmutableList
import com.nytimes.android.external.register.APIOverrides.Companion.RESULT_DEFAULT
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyListOf
import org.mockito.Matchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class BillingServicesSubImplTest {

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Mock
    private lateinit var buyIntentBundleBuilder: BuyIntentBundleBuilder

    @Mock
    private lateinit var skuDetailsBundleBuilder: SkuDetailsBundleBuilder

    @Mock
    private lateinit var purchasesBundleBuilder: PurchasesBundleBuilder

    @Mock
    private lateinit var buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder

    private lateinit var testObject: BillingServiceStubImpl

    private val type = GoogleUtil.BILLING_TYPE_IAP
    private val expected = Bundle()

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
        `when`(buyIntentToReplaceSkusBundleBuilder.newBuilder()).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.developerPayload(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.packageName(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.newSku(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.oldSkus(anyListOf(String::class.java)))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.type(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder)
        `when`(buyIntentToReplaceSkusBundleBuilder.build()).thenReturn(expected)

        testObject = BillingServiceStubImpl(apiOverrides,
                buyIntentBundleBuilder,
                skuDetailsBundleBuilder,
                purchasesBundleBuilder,
                mock(ConsumePurchaseResponse::class.java),
                buyIntentToReplaceSkusBundleBuilder)
    }

    @Test
    fun testIsBillingSupportedDefault() {
        `when`(apiOverrides.isBillingSupportedResponse).thenReturn(RESULT_DEFAULT)

        val actual = testObject.isBillingSupported(API_VERSION, PACKAGE_NAME, type)

        assertThat(actual).isEqualTo(GoogleUtil.RESULT_OK)
    }

    @Test
    fun testIsBillingSupportedNonDefault() {
        val expected = GoogleUtil.RESULT_BILLING_UNAVAILABLE
        `when`(apiOverrides.isBillingSupportedResponse).thenReturn(expected)

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
                ImmutableList.of(SKU), "sku2", type, DEVELOPER_PAYLOAD)

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
