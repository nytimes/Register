package com.nytimes.android.external.registerlib;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class GoogleProductResponseTest {


    @Test
    public void testGoodString(){

        String json2 = "{ \"description\": " +
                "\"Unlimited article access, anytime, anywhere. Save with an annual subscription.\", " +
                "\"introductoryPrice\": \"$89.99\", \"introductoryPriceCycles\": 1, " +
                "\"introductoryPriceMicros\": 89990000, " +
                "\"introductoryPricePeriod\": \"P1Y\", \"price\": \"$129.99\", \"price_amount_micros\": 129990000, " +
                "\"price_currency_code\": \"USD\", \"productId\": \"com.nytimes.googleplay.bundle.xpass.v1.yearly\", " +
                "\"subscriptionPeriod\": \"P1Y\", \"title\": \"NYT Basic Access: Annual\", \"type\": \"subs\" }";
        GoogleProductResponse response = GoogleProductResponse.fromJson(json2);
        assertNotNull(response);
    }
    @Test
    public void testBadString(){
        String bad = "{ \"description\": " +
                "\"Basic access features, plus NYT Crossword, Cooking, and one bonus \\nsubscription.\", " +
                "\"freeTrialPeriod\": \"P1W\", \"price\": \"$24.99\", \"price_amount_micros\": 24990000, " +
                "\"price_currency_code\": \"USD\", " +
                "\"productId\": \"com.nytimes.googleplay.bundle.maxadacr.v1.monthly\", " +
                "\"subscriptionPeriod\": \"P1M\", " +
                "\"title\": \"NYT All Access (NYTimes - Latest News)\", \"type\": \"subs\" }";

        GoogleProductResponse response = GoogleProductResponse.fromJson(bad);
        assertNotNull(response);
    }

    @Test
    public void testNullInt() throws JSONException {
        JSONObject obj = new JSONObject("{\"hello\": \"world\", \"myint\": 2}");
        Integer field = JsonHelper.getFieldAsIntOrNull(obj, "missingField");
        Integer foundField = JsonHelper.getFieldAsIntOrNull(obj, "myint");

        assertNull(field);
        assertNotNull(foundField);
        assertEquals(2, foundField.intValue());
    }

}
