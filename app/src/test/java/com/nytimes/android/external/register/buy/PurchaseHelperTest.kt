package com.nytimes.android.external.register.buy

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.register.model.ConfigSku
import com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_IAP
import com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_SUBSCRIPTION
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PurchaseHelperTest {
    private val config: Config = mock()
    private val apiOverrides: APIOverrides = mock()
    private val purchases: Purchases = mock()
    private val testSKU = "testSKU"
    private val purchaseData = PurchaseData(
        sku = testSKU,
        itemtype = "test"
    )
    private val mockSKU: ConfigSku = mock()
    private lateinit var testObject: PurchaseHelper

    @Before
    fun setup() {
        testObject = PurchaseHelper(config, apiOverrides, purchases)
        whenever(apiOverrides.usersResponse).thenReturn("testResponse")
        whenever(config.skus).thenReturn(mapOf(Pair(testSKU, mockSKU)))
        whenever(mockSKU.type).thenReturn("subs")
    }

    @Test
    fun `onBuy - subscription purchase - adds auto renewing subscription purchase`() {
        // Setup
        whenever(mockSKU.type).thenReturn(BILLING_TYPE_SUBSCRIPTION)

        // Exercise
        val result = testObject.onBuy(purchaseData, System.currentTimeMillis())

        // Verify
        assertThat(result.inAppPurchaseData.isAutoRenewing()).isTrue()
    }

    @Test
    fun `onBuy - inapp purchase - adds non auto renewing purchase`() {
        // Setup
        whenever(mockSKU.type).thenReturn(BILLING_TYPE_IAP)

        // Exercise
        val result = testObject.onBuy(purchaseData, System.currentTimeMillis())

        // Verify
        assertThat(result.inAppPurchaseData.isAutoRenewing()).isFalse()
    }
}