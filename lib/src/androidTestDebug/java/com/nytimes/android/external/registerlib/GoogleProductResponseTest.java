package com.nytimes.android.external.registerlib;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class GoogleProductResponseTest {

    @Test
    public void testGoodString(){
        String goodJSONSample = "{ \"description\": " +
                "\"Buy now!!\", " +
                "\"introductoryPrice\": \"$79.99\", \"introductoryPriceCycles\": 1, " +
                "\"introductoryPriceMicros\": 79990000, " +
                "\"introductoryPricePeriod\": \"P1Y\", \"price\": \"$129.99\", \"price_amount_micros\": 129990000, " +
                "\"price_currency_code\": \"USD\", \"productId\": \"com.somecompany.googleplay.bundle.yearly\", " +
                "\"subscriptionPeriod\": \"P1Y\", \"title\": \"Yearly Option\", \"type\": \"subs\" }";
        GoogleProductResponse response = GoogleProductResponse.fromJson(goodJSONSample);
        assertThat(response).isNotNull();
    }

    @Test
    public void testMissingIntField(){
        String jsonMissingFields = "{ \"description\": " +
                "\"Basic access , " +
                "\"freeTrialPeriod\": \"P1W\", \"price\": \"$34.99\", \"price_amount_micros\": 34990000, " +
                "\"price_currency_code\": \"USD\", " +
                "\"productId\": \"com.somecompany.googleplay.bundle.monthly\", " +
                "\"subscriptionPeriod\": \"P1M\", " +
                "\"title\": \"Premium Access\", \"type\": \"subs\" }";

        GoogleProductResponse response = GoogleProductResponse.fromJson(jsonMissingFields);
        assertThat(response).isNotNull();
    }

    @Test
    public void testNullInt() throws JSONException {
        JSONObject obj = new JSONObject("{\"hello\": \"world\", \"myint\": 2}");
        Integer field = JsonHelper.getFieldAsIntOrNull(obj, "missingField");
        Integer foundField = JsonHelper.getFieldAsIntOrNull(obj, "myint");

        assertThat(field).isNull();
        assertThat(foundField).isNotNull();
        assertThat(foundField.intValue()).isEqualTo(2);
    }

}
