package com.nytimes.android.external.register

import android.content.Context
import com.nytimes.android.external.register.APIOverrides.PREF_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class APIOverridesTest {

    private lateinit var testObject: APIOverrides

    @Before
    fun setUp() {
        val sharedPreferences = RuntimeEnvironment.application
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        testObject = APIOverrides(sharedPreferences)
    }

    @Test
    fun testIsBillingSupported() {
        testObject.isBillingSupportedResponse = IS_BILLING_RESPONSE
        assertThat(testObject.isBillingSupportedResponse)
                .isEqualTo(IS_BILLING_RESPONSE)
    }

    @Test
    fun testBuyIntentResponse() {
        testObject.getBuyIntentResponse = GET_BUY_INTENT_RESPONSE
        assertThat(testObject.getBuyIntentResponse)
                .isEqualTo(GET_BUY_INTENT_RESPONSE)
    }

    @Test
    fun testBuyResponse() {
        testObject.buyResponse = BUY_RESPONSE
        assertThat(testObject.buyResponse)
                .isEqualTo(BUY_RESPONSE)
    }

    @Test
    fun testPurchasesResponse() {
        testObject.getPurchasesResponse = GET_PURCHASES_RESPONSE
        assertThat(testObject.getPurchasesResponse)
                .isEqualTo(GET_PURCHASES_RESPONSE)
    }

    @Test
    fun testGetSkuDetailsResponse() {
        testObject.getSkuDetailsResponse = GET_SKU_DETAILS_RESPONSE
        assertThat(testObject.getSkuDetailsResponse)
                .isEqualTo(GET_SKU_DETAILS_RESPONSE)
    }

    @Test
    fun testConsumePurchaseResponse() {
        testObject.consumePurchaseResponse = CONSUME_PURCHASE_RESPONSE
        assertThat(testObject.consumePurchaseResponse)
                .isEqualTo(CONSUME_PURCHASE_RESPONSE)
    }

    @Test
    fun testGetUsersResponse() {
        testObject.setUsersReponse(GET_USERS_RESPONSE)
        assertThat(testObject.usersResponse)
                .isEqualTo(GET_USERS_RESPONSE)
    }

    companion object {
        private const val IS_BILLING_RESPONSE = -100
        private const val GET_BUY_INTENT_RESPONSE = -101
        private const val BUY_RESPONSE = -102
        private const val GET_PURCHASES_RESPONSE = -103
        private const val GET_SKU_DETAILS_RESPONSE = -104
        private const val CONSUME_PURCHASE_RESPONSE = -105
        private const val GET_USERS_RESPONSE = "myfun@user.com"
    }
}
