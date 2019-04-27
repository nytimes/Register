package com.nytimes.android.external.register

import com.nytimes.android.external.registerlib.GoogleUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class APIOverridesDelegateTest {

    private lateinit var testObject: APIOverridesDelegate

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        testObject = APIOverridesDelegate(apiOverrides)
    }

    @Test
    fun setApiOverridesValueWithValidIdCallsApiOverrides() {
        val mockConfig = mock(ConfigResponse::class.java)
        `when`(mockConfig.responseCode()).thenReturn(-1)

        // Valid entries
        testObject.setApiOverridesValue(R.id.isBillingSupported, mockConfig)
        testObject.setApiOverridesValue(R.id.getBuyIntent, mockConfig)
        testObject.setApiOverridesValue(R.id.buy, mockConfig)
        testObject.setApiOverridesValue(R.id.getPurchases, mockConfig)
        testObject.setApiOverridesValue(R.id.getSkuDetails, mockConfig)
        testObject.setApiOverridesValue(R.id.getBuyIntentToReplaceSkus, mockConfig)

        // verify valid interactions
        verify<APIOverrides>(apiOverrides, times(1)).isBillingSupportedResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(1)).getBuyIntentResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(1)).buyResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(1)).getPurchasesResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(1)).getSkuDetailsResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(1)).getBuyIntentToReplaceSkusResponse = RESULT_DEFAULT
    }

    @Test
    fun setApiOverridesValueWithInvalidIdCallsNothing() {
        val mockConfig = mock(ConfigResponse::class.java)
        `when`(mockConfig.responseCode()).thenReturn(-1)

        // Valid entries
        testObject.setApiOverridesValue(android.R.id.hint, mockConfig)
        testObject.setApiOverridesValue(android.R.id.home, mockConfig)
        testObject.setApiOverridesValue(android.R.id.redo, mockConfig)
        testObject.setApiOverridesValue(android.R.id.accessibilityActionContextClick, mockConfig)
        testObject.setApiOverridesValue(android.R.id.candidatesArea, mockConfig)
        testObject.setApiOverridesValue(android.R.id.keyboardView, mockConfig)

        // verify only valid interactions
        verify<APIOverrides>(apiOverrides, times(0)).isBillingSupportedResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(0)).getBuyIntentResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(0)).buyResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(0)).getPurchasesResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(0)).getSkuDetailsResponse = RESULT_DEFAULT
        verify<APIOverrides>(apiOverrides, times(0)).getBuyIntentToReplaceSkusResponse = RESULT_DEFAULT
    }

    @Test
    fun getApiOverridesValueWithValidIdCallsGetters() {
        val mockConfig = mock(ConfigResponse::class.java)
        `when`(mockConfig.responseCode()).thenReturn(-1)

        // Valid entries
        testObject.getApiOverridesValue(R.id.isBillingSupported)
        testObject.getApiOverridesValue(R.id.getBuyIntent)
        testObject.getApiOverridesValue(R.id.buy)
        testObject.getApiOverridesValue(R.id.getPurchases)
        testObject.getApiOverridesValue(R.id.getSkuDetails)
        testObject.getApiOverridesValue(R.id.getBuyIntentToReplaceSkus)

        // verify only valid interactions
        verify<APIOverrides>(apiOverrides, times(1)).isBillingSupportedResponse
        verify<APIOverrides>(apiOverrides, times(1)).getBuyIntentResponse
        verify<APIOverrides>(apiOverrides, times(1)).buyResponse
        verify<APIOverrides>(apiOverrides, times(1)).getPurchasesResponse
        verify<APIOverrides>(apiOverrides, times(1)).getSkuDetailsResponse
        verify<APIOverrides>(apiOverrides, times(1)).getBuyIntentToReplaceSkusResponse
    }

    @Test
    fun getApiOverridesValueWithInvalidIdReturnsNegativeOne() {
        val mockConfig = mock(ConfigResponse::class.java)
        `when`(mockConfig.responseCode()).thenReturn(-1)

        // Valid entries
        testObject.getApiOverridesValue(android.R.id.hint)
        testObject.getApiOverridesValue(android.R.id.home)
        testObject.getApiOverridesValue(android.R.id.redo)
        testObject.getApiOverridesValue(android.R.id.accessibilityActionContextClick)
        testObject.getApiOverridesValue(android.R.id.candidatesArea)
        testObject.getApiOverridesValue(android.R.id.keyboardView)

        // verify only valid interactions
        verify<APIOverrides>(apiOverrides, times(0)).isBillingSupportedResponse
        verify<APIOverrides>(apiOverrides, times(0)).getBuyIntentResponse
        verify<APIOverrides>(apiOverrides, times(0)).buyResponse
        verify<APIOverrides>(apiOverrides, times(0)).getPurchasesResponse
        verify<APIOverrides>(apiOverrides, times(0)).getSkuDetailsResponse
        verify<APIOverrides>(apiOverrides, times(0)).getBuyIntentToReplaceSkusResponse
    }

    @Test
    fun getApiOverridesValueWithInvalidIdReturnsCorrectValue() {
        `when`(apiOverrides.isBillingSupportedResponse).thenReturn(GoogleUtil.RESULT_BILLING_UNAVAILABLE)
        val isBillingSupported = testObject.getApiOverridesValue(R.id.isBillingSupported)
        assertThat(isBillingSupported).isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE)

        `when`(apiOverrides.getBuyIntentResponse).thenReturn(GoogleUtil.RESULT_ITEM_NOT_OWNED)
        val getBuyIntent = testObject.getApiOverridesValue(R.id.getBuyIntent)
        assertThat(getBuyIntent).isEqualTo(GoogleUtil.RESULT_ITEM_NOT_OWNED)

        `when`(apiOverrides.buyResponse).thenReturn(GoogleUtil.RESULT_DEVELOPER_ERROR)
        val buy = testObject.getApiOverridesValue(R.id.buy)
        assertThat(buy).isEqualTo(GoogleUtil.RESULT_DEVELOPER_ERROR)

        `when`(apiOverrides.getPurchasesResponse).thenReturn(GoogleUtil.RESULT_ERROR)
        val getPurchases = testObject.getApiOverridesValue(R.id.getPurchases)
        assertThat(getPurchases).isEqualTo(GoogleUtil.RESULT_ERROR)

        `when`(apiOverrides.getSkuDetailsResponse).thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
        val getSkuDetails = testObject.getApiOverridesValue(R.id.getSkuDetails)
        assertThat(getSkuDetails).isEqualTo(GoogleUtil.RESULT_ITEM_ALREADY_OWNED)

        `when`(apiOverrides.getBuyIntentToReplaceSkusResponse).thenReturn(GoogleUtil.RESULT_OK)
        val getBuyIntentToReplaceSkus = testObject.getApiOverridesValue(R.id.getBuyIntentToReplaceSkus)
        assertThat(getBuyIntentToReplaceSkus).isEqualTo(GoogleUtil.RESULT_OK)
    }

    companion object {
        /**
         *  no user override
         */
        private const val RESULT_DEFAULT = -1
    }
}
