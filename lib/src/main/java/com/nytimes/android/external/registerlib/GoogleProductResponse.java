package com.nytimes.android.external.registerlib;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nytimes.android.external.registerlib.JsonHelper.addToObj;
import static com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull;
import static com.nytimes.android.external.registerlib.JsonHelper.getFieldAsIntOrNull;
import static com.nytimes.android.external.registerlib.JsonHelper.getFieldAsStringOrNull;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class GoogleProductResponse {
    private static final String TAG = GoogleProductResponse.class.getSimpleName();
    private static final String FLD_PRODUCT_ID = "productId";
    private static final String FLD_ITEM_TYPE = "type";
    private static final String FLD_PRICE = "price";
    private static final String FLD_TITLE = "title";
    private static final String FLD_DESCRIPTION = "description";
    private static final String FLD_PRICE_AMT_MICROS = "price_amount_micros";
    private static final String FLD_PRICE_CURR_CODE = "price_currency_code";

    String productId;
    String itemType;
    String price;
    String title;
    String description;
    int priceAmountMicros;
    String priceCurrencyCode;

    public static GoogleProductResponse fromJson(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
        } catch (JSONException exc) {
            Log.e(TAG, "Error creating json", exc);
        }
        return new GoogleProductResponse.Builder()
                .productId(getFieldAsStringOrNull(obj, FLD_PRODUCT_ID))
                .itemType(getFieldAsStringOrNull(obj, FLD_ITEM_TYPE))
                .price(getFieldAsStringOrNull(obj, FLD_PRICE))
                .title(getFieldAsStringOrNull(obj, FLD_TITLE))
                .description(getFieldAsStringOrNull(obj, FLD_DESCRIPTION))
                .priceAmountMicros(getFieldAsIntOrNull(obj, FLD_PRICE_AMT_MICROS))
                .priceCurrencyCode(getFieldAsStringOrNull(obj, FLD_PRICE_CURR_CODE))
                .build();
    }

    public static String toJson(GoogleProductResponse googleProductResponse) {
        JSONObject obj = new JSONObject();
        addToObjIfNotNull(FLD_PRODUCT_ID, googleProductResponse.productId(), obj);
        addToObjIfNotNull(FLD_ITEM_TYPE, googleProductResponse.itemType(), obj);
        addToObjIfNotNull(FLD_PRICE, googleProductResponse.price(), obj);
        addToObjIfNotNull(FLD_TITLE, googleProductResponse.title(), obj);
        addToObjIfNotNull(FLD_DESCRIPTION, googleProductResponse.description(), obj);
        addToObj(FLD_PRICE_AMT_MICROS, googleProductResponse.priceAmountMicros(), obj);
        addToObjIfNotNull(FLD_PRICE_CURR_CODE, googleProductResponse.priceCurrencyCode(), obj);
        return obj.toString();
    }

    public static class Builder {
        GoogleProductResponse builderObject = new GoogleProductResponse();
        public Builder() {

        }

        public Builder productId(String productId) {
            builderObject.productId = productId;
            return this;
        }

        public Builder itemType(String itemType) {
            builderObject.itemType = itemType;
            return this;
        }

        public Builder price(String price) {
            builderObject.price = price;
            return this;
        }

        public Builder title(String title) {
            builderObject.title = title;
            return this;
        }

        public Builder description(String description) {
            builderObject.description = description;
            return this;
        }

        public Builder priceAmountMicros(int priceAmountMicros) {
            builderObject.priceAmountMicros = priceAmountMicros;
            return this;
        }

        public Builder priceCurrencyCode(String priceCurrencyCode) {
            builderObject.priceCurrencyCode = priceCurrencyCode;
            return this;
        }

        public GoogleProductResponse build() {
            return builderObject;
        }
    }

    public String productId() {
        return productId;
    }

    public String itemType() {
        return itemType;
    }


    public String price() {
        return price;
    }


    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public int priceAmountMicros() {
        return priceAmountMicros;
    }

    public String priceCurrencyCode() {
        return priceCurrencyCode;
    }
}
