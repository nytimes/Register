package com.nytimes.android.external.registerlib

import org.json.JSONException
import org.json.JSONObject
import org.junit.Test

import org.assertj.core.api.Assertions.assertThat


class GoogleProductResponseTest {

    @Test
    fun testGoodString() {
        val goodJSONSample = "{ \"description\": " +
                "\"Buy now!!\", " +
                "\"introductoryPrice\": \"$79.99\", \"introductoryPriceCycles\": 1, " +
                "\"introductoryPriceMicros\": 79990000, " +
                "\"introductoryPricePeriod\": \"P1Y\", \"price\": \"$129.99\", \"price_amount_micros\": 129990000, " +
                "\"price_currency_code\": \"USD\", \"productId\": \"com.somecompany.googleplay.bundle.yearly\", " +
                "\"subscriptionPeriod\": \"P1Y\", \"title\": \"Yearly Option\", \"type\": \"subs\" }"
        val response = GoogleProductResponse.fromJson(goodJSONSample)
        assertThat(response).isNotNull()
    }

    @Test
    fun testMissingIntField() {
        val jsonMissingFields = "{ \"description\": " +
                "\"Basic access , " +
                "\"freeTrialPeriod\": \"P1W\", \"price\": \"$34.99\", \"price_amount_micros\": 34990000, " +
                "\"price_currency_code\": \"USD\", " +
                "\"productId\": \"com.somecompany.googleplay.bundle.monthly\", " +
                "\"subscriptionPeriod\": \"P1M\", " +
                "\"title\": \"Premium Access\", \"type\": \"subs\" }"

        val response = GoogleProductResponse.fromJson(jsonMissingFields)
        assertThat(response).isNotNull()
    }

    @Test
    @Throws(JSONException::class)
    fun testNullInt() {
        val obj = JSONObject("{\"hello\": \"world\", \"myint\": 2}")
        val field = JsonHelper.getFieldAsIntOrZero(obj, "missingField")
        val foundField = JsonHelper.getFieldAsIntOrZero(obj, "myint")

        assertThat(field).isNotNull()
        assertThat(foundField).isNotNull()
        assertThat(foundField).isEqualTo(2)
    }

}
