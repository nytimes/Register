package com.nytimes.android.external.register.bundle

import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.register.model.ConfigSku
import com.nytimes.android.external.register.model.ImmutableConfigSku
import com.nytimes.android.external.registerlib.GoogleUtil
import io.reactivex.exceptions.Exceptions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

@RunWith(RobolectricTestRunner::class)
class SkuDetailsBundleBuilderTest {

    private lateinit var testObject: SkuDetailsBundleBuilder

    @Mock
    private lateinit var apiOverrides: APIOverrides

    @Mock
    private lateinit var config: Config

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val packageName = "com.my.pkg"
        val description = "some description"
        val title = "caps for sale"
        val price = "1.98"

        testObject = SkuDetailsBundleBuilder(apiOverrides, Optional.of(config))
        `when`(config.skus()).thenReturn(ImmutableMap.Builder<String, ConfigSku>()
                .put(SKU1, ImmutableConfigSku.builder()
                        .type(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .put(SKU2, ImmutableConfigSku.builder()
                        .type(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .build())
    }

    @Test
    fun testBundleOK() {
        `when`(apiOverrides.getSkuDetailsResponse).thenReturn(GoogleUtil.RESULT_OK)

        val bundle = testObject.newBuilder()
                .skus(ImmutableList.of(SKU1, SKU2), TYPE)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK)
        val detailsList = bundle.getStringArrayList(GoogleUtil.DETAILS_LIST)
        try {
            JSONAssert.assertEquals(detailsList[0],
                    "{\"type\":\"subs\",\"productId\":\"sku1\",\"price\":\"$1.98\"," +
                            "\"description\":\"some description\",\"title\":\"caps for sale\"," +
                            "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}",
                    JSONCompareMode.LENIENT)
            JSONAssert.assertEquals(detailsList[1],
                    "{\"type\":\"subs\",\"productId\":\"sku2\",\"price\":\"$1.98\"," +
                            "\"description\":\"some description\",\"title\":\"caps for sale\"," +
                            "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}",
                    JSONCompareMode.LENIENT)
        } catch (e: JSONException) {
            throw Exceptions.propagate(e)
        }

    }

    @Test
    fun testBundleNotOK() {
        `when`(apiOverrides.getSkuDetailsResponse).thenReturn(GoogleUtil.RESULT_ERROR)

        val bundle = testObject.newBuilder()
                .skus(ImmutableList.of(SKU1, SKU2), TYPE)
                .build()

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR)
        assertThat(bundle.getStringArrayList(GoogleUtil.DETAILS_LIST))
                .isNull()
    }

    @Test
    fun testRawResponseCode() {
        testObject.rawResponseCode()
        verify<APIOverrides>(apiOverrides).getSkuDetailsResponse
    }

    companion object {
        private const val SKU1 = "sku1"
        private const val SKU2 = "sku2"
        private const val TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION
    }
}

