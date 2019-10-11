package com.nytimes.android.external.register

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import java.util.*

@RunWith(AndroidJUnit4::class)
class BuyActivityTest {

    private lateinit var testObject: BuyActivity
    private lateinit var shadowActivity: ShadowActivity
    private val configSku = ConfigSku(TYPE, PRICE, TITLE, DESCRIPTION, PACKAGE_NAME)
    private lateinit var configSkuMapBuilder: MutableMap<String, ConfigSku>
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

    private val apiOverrides: APIOverrides = mock()
    private val purchases: Purchases = mock()
    private val purchasesLists: PurchasesLists = mock()
    private val config: Config = mock()
    private val signer: Signer = mock()

    @Before
    fun setUp() {
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
        testObject.config = config
        testObject.signer = signer
        shadowActivity = shadowOf(testObject)

        whenever(purchases.getPurchasesLists(TYPE, CONTINUATION_TOKEN)).thenReturn(purchasesLists)
        configSkuMapBuilder = mutableMapOf()
    }

    @Test
    fun testBuyResponseOK() {
        initTestObject(false)

        whenever(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        whenever(purchases.getReceiptsForSkus(setOf(SKU), TYPE)).thenReturn(setOf())
        configSkuMapBuilder.put(SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

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
        whenever(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        whenever(purchases.getReceiptsForSkus(setOf(SKU), TYPE)).thenReturn(setOf())
        configSkuMapBuilder.put(sku2, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

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

        whenever(apiOverrides.buyResponse).thenReturn(APIOverrides.RESULT_DEFAULT)
        whenever(purchases.getReceiptsForSkus(setOf(SKU), TYPE)).thenReturn(setOf(RECEIPT))
        configSkuMapBuilder.put(SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

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

        whenever(apiOverrides.buyResponse).thenReturn(GoogleUtil.RESULT_ERROR)

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

        whenever(apiOverrides.usersResponse).thenReturn(USER)
        configSkuMapBuilder.put(SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)
        val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
        whenever(purchases.addPurchase(inAppPurchaseDataStr, TYPE)).thenReturn(true)
        whenever(signer.signData(inAppPurchaseDataStr)).thenReturn(signedData)

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
        whenever(apiOverrides.replaceResponse)
                .thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
        whenever(purchases.getInAppPurchaseData(TYPE)).thenReturn(setOf(inAppPurchaseData))

        initTestObject(true)
        configSkuMapBuilder.put(NEW_SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

        controller.start()
        testObject.findViewById<View>(R.id.buy_button).callOnClick()

        assertThat(shadowActivity.resultCode).isEqualTo(RESULT_OK)
        val resultIntent = shadowActivity.resultIntent
        val actual = resultIntent.getIntExtra(GoogleUtil.RESPONSE_CODE, -1)

        assertThat(actual).isEqualTo(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
    }

    @Test
    fun testHandleAlreadyOwnedInvalid() {
        whenever(apiOverrides.replaceResponse)
                .thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
        whenever(purchases.getInAppPurchaseData(TYPE)).thenReturn(setOf())

        initTestObject(true)
        configSkuMapBuilder.put(SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

        controller.start()
        testObject.findViewById<View>(R.id.buy_button).callOnClick()

        assertThat(shadowActivity.resultCode).isEqualTo(RESULT_CANCELED)
        assertThat(shadowActivity.resultIntent).isNull()
    }

    @Test
    fun onBackPressedResultsOk() {
        initTestObject(false)
        configSkuMapBuilder.put(SKU, configSku)
        whenever(config.skus).thenReturn(configSkuMapBuilder)

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
