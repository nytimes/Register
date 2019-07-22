package com.nytimes.android.external.register

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.common.base.Optional
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.nytimes.android.external.register.BuyActivity.Companion.ERROR_FMT
import com.nytimes.android.external.register.BuyActivity.Companion.PRICE_FMT
import com.nytimes.android.external.register.BuyActivity.Companion.RECEIPT_FMT
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.register.model.ConfigSku
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.InAppPurchaseData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import java.util.*

@RunWith(RobolectricTestRunner::class)
class BuyActivityTest {

    private lateinit var testObject: BuyActivity
    private lateinit var shadowActivity: ShadowActivity
    private val configSku = ConfigSku(TYPE, PRICE, TITLE, DESCRIPTION, PACKAGE_NAME)
    private lateinit var configSkuMapBuilder: ImmutableMap.Builder<String, ConfigSku>
    private val inAppPurchaseData = InAppPurchaseData.Builder()
            .orderId(java.lang.Long.toString(CURRENT_TIME_MS))
            .packageName(PACKAGE_NAME)
            .productId(SKU)
            .purchaseTime(java.lang.Long.toString(CURRENT_TIME_MS))
            .developerPayload(DEVELOPER_PAYLOAD)
            .purchaseToken(String.format(Locale.getDefault(), RECEIPT_FMT, USER, CURRENT_TIME_MS))
            .purchaseState(PURCHASE_STATE)
            .build()

    private lateinit var controller: ActivityController<*>

    @Mock
    private lateinit var apiOverrides: APIOverrides
    @Mock
    private lateinit var purchases: Purchases
    @Mock
    private lateinit var purchasesLists: PurchasesLists
    @Mock
    private lateinit var config: Config
    @Mock
    private lateinit var signer: Signer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    private fun initTestObject(isReplace: Boolean) {
        val intent = Intent(RuntimeEnvironment.application, TestBuyActivity::class.java)

        intent.putExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE, TYPE)
        intent.putExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD, DEVELOPER_PAYLOAD)
        if (isReplace) {
            intent.putExtra(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU, NEW_SKU)
            intent.putStringArrayListExtra(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS,
                    arrayListOf(NEW_SKU))
        } else {
            intent.putExtra(BuyIntentBundleBuilder.EX_SKU, SKU)
        }

        controller = Robolectric.buildActivity(TestBuyActivity::class.java, intent).create()
        testObject = controller.get() as BuyActivity
        testObject.apiOverrides = apiOverrides
        testObject.purchases = purchases
        testObject.config = Optional.of(config)
        testObject.signer = signer
        shadowActivity = shadowOf(testObject)

        `when`(purchases.getPurchasesLists(TYPE, CONTINUATION_TOKEN)).thenReturn(purchasesLists)
        configSkuMapBuilder = ImmutableMap.builder()
    }

    @Test
    fun testBuyResponseOK() {
        initTestObject(false)

        `when`(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        `when`(purchases.getReceiptsForSkus(ImmutableSet.of(SKU), TYPE)).thenReturn(ImmutableSet.of())
        configSkuMapBuilder.put(SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()

        val titleTextView = testObject.findViewById<View>(R.id.buy_title) as TextView
        val summaryTextView = testObject.findViewById<View>(R.id.buy_summary) as TextView
        val priceTextView = testObject.findViewById<View>(R.id.buy_price) as TextView
        val buyButton = testObject.findViewById<View>(R.id.buy_button) as Button

        assertThat(titleTextView.text).isEqualTo(TITLE)
        assertThat(summaryTextView.text).isEqualTo(DESCRIPTION)
        assertThat(priceTextView.text).isEqualTo(String.format(PRICE_FMT, PRICE))
        assertThat(buyButton.text).isEqualTo(getStringResource(R.string.buy))
    }

    @Test
    fun testItemUnavailable() {
        initTestObject(false)

        val sku2 = "sku2"
        `when`(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        `when`(purchases.getReceiptsForSkus(ImmutableSet.of(SKU), TYPE)).thenReturn(ImmutableSet.of())
        configSkuMapBuilder.put(sku2, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()

        val actualTitle = (testObject.findViewById<View>(R.id.buy_title) as TextView).text
        val expectedTitle = getStringResource(R.string.error)
        assertThat(actualTitle).isEqualTo(expectedTitle)

        val actualSummary = (testObject.findViewById<View>(R.id.buy_summary) as TextView).text
        val expectedSummary = getStringResource(R.string.item_not_found)
        assertThat(actualSummary).isEqualTo(expectedSummary)
    }

    @Test
    fun testItemAlreadyOwned() {
        initTestObject(false)

        `when`(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        `when`(purchases.getReceiptsForSkus(ImmutableSet.of(SKU), TYPE)).thenReturn(ImmutableSet.of(RECEIPT))
        configSkuMapBuilder.put(SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()

        val actualTitle = (testObject.findViewById<View>(R.id.buy_title) as TextView).text
        val expectedTitle = getStringResource(R.string.error)
        assertThat(actualTitle).isEqualTo(expectedTitle)

        val actualSummary = (testObject.findViewById<View>(R.id.buy_summary) as TextView).text
        val expectedSummary = getStringResource(R.string.item_already_owned)
        assertThat(actualSummary).isEqualTo(expectedSummary)
    }

    @Test
    fun testError() {
        initTestObject(false)

        `when`(apiOverrides.buyResponse).thenReturn(GoogleUtil.RESULT_ERROR)

        controller.start()

        val errorTitle = getStringResource(R.string.error)

        val actualTitle = (testObject.findViewById<View>(R.id.buy_title) as TextView).text
        assertThat(actualTitle).isEqualTo(errorTitle)

        val actualSummary = (testObject.findViewById<View>(R.id.buy_summary) as TextView).text
        val expectedSummary = String.format(Locale.getDefault(), ERROR_FMT,
                errorTitle, GoogleUtil.RESULT_ERROR)
        assertThat(actualSummary).isEqualTo(expectedSummary)
    }

    @Test
    @Throws(Exception::class)
    fun testHandleBuy() {
        val signedData = "signedData"
        initTestObject(false)

        `when`(apiOverrides.usersResponse).thenReturn(USER)
        configSkuMapBuilder.put(SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())
        val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
        `when`(purchases.addPurchase(inAppPurchaseDataStr, TYPE)).thenReturn(true)
        `when`(signer.signData(inAppPurchaseDataStr)).thenReturn(signedData)

        controller.start()
        testObject.currentTimeMillis = CURRENT_TIME_MS
        testObject.findViewById<View>(R.id.buy_button).callOnClick()

        assertThat(shadowActivity.resultCode)
                .isEqualTo(RESULT_OK)
        val resultIntent = shadowActivity.resultIntent
        assertThat(resultIntent.getIntExtra(GoogleUtil.RESPONSE_CODE, APIOverrides.RESULT_DEFAULT))
                .isEqualTo(GoogleUtil.RESULT_OK)
        assertThat(resultIntent.getStringExtra(GoogleUtil.INAPP_PURCHASE_DATA))
                .isEqualTo(inAppPurchaseDataStr)
        assertThat(resultIntent.getStringExtra(GoogleUtil.INAPP_DATA_SIGNATURE)).isEqualTo(signedData)

        verify(purchases).addPurchase(inAppPurchaseDataStr, TYPE)
    }

    @Test
    fun testHandleAlreadyOwnedValid() {
        `when`(apiOverrides.replaceResponse)
                .thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
        `when`(purchases.getInAppPurchaseData(TYPE)).thenReturn(ImmutableSet.of(inAppPurchaseData))

        initTestObject(true)
        configSkuMapBuilder.put(NEW_SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()
        testObject.findViewById<View>(R.id.buy_button).callOnClick()

        assertThat(shadowActivity.resultCode).isEqualTo(RESULT_OK)
        val resultIntent = shadowActivity.resultIntent
        val actual = resultIntent.getIntExtra(GoogleUtil.RESPONSE_CODE, -1)

        assertThat(actual).isEqualTo(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
    }

    @Test
    fun testHandleAlreadyOwnedInvalid() {
        `when`(apiOverrides.replaceResponse)
                .thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
        `when`(purchases.getInAppPurchaseData(TYPE)).thenReturn(ImmutableSet.of())

        initTestObject(true)
        configSkuMapBuilder.put(SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()
        testObject.findViewById<View>(R.id.buy_button).callOnClick()

        assertThat(shadowActivity.resultCode).isEqualTo(RESULT_CANCELED)
        assertThat(shadowActivity.resultIntent).isNull()
    }

    @Test
    fun onBackPressedResultsOk() {
        initTestObject(false)
        configSkuMapBuilder.put(SKU, configSku)
        `when`(config.skus).thenReturn(configSkuMapBuilder.build())

        controller.start()
        testObject.onBackPressed()

        assertThat(shadowActivity.resultCode).isEqualTo(RESULT_OK)
    }

    private fun getStringResource(id: Int): String {
        return RuntimeEnvironment.application.resources.getString(id)
    }

    internal class TestBuyActivity : BuyActivity() {

        override fun inject() {
            //intentionally blank
        }
    }

    companion object {
        private const val SKU = "sku1"
        private const val RECEIPT = "myfun@user.com.registerToken1234567"
        private const val TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION
        private const val CONTINUATION_TOKEN = ""
        private const val DEVELOPER_PAYLOAD = "devPayload"
        private const val DESCRIPTION = "some description"
        private const val PACKAGE_NAME = "com.my.pkg"
        private const val PRICE = "1.98"
        private const val TITLE = "caps for sale"
        private const val USER = "myfun@user.com"
        private const val PURCHASE_STATE = "0"
        private const val NEW_SKU = "sku2"
        private const val CURRENT_TIME_MS = 1234567L
    }
}
