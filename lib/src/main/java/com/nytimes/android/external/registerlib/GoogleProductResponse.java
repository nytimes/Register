package com.nytimes.android.external.registerlib;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nytimes.android.external.registerlib.JsonHelper.addToObj;
import static com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull;
import static com.nytimes.android.external.registerlib.JsonHelper.getFieldAsIntOrZero;
import static com.nytimes.android.external.registerlib.JsonHelper.getFieldAsLongOrZero;
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
    private static final String FLD_SUBSCRIPTION_PERIOD = "subscriptionPeriod";
    private static final String FLD_FREE_TRIAL_PERIOD = "freeTrialPeriod";
    private static final String FLD_INTRODUCTORY_PRICE = "introductoryPrice";
    private static final String FLD_INTRODUCTORY_PRICE_AMT_MICROS = "introductoryPriceAmountMicros";
    private static final String FLD_INTRODUCTORY_PRICE_PERIOD = "introductoryPricePeriod";
    private static final String FLD_INTRODUCTORY_PRICE_CYCLES = "introductoryPriceCycles";

    String productId;
    String itemType;
    String price;
    String title;
    String description;
    long priceAmountMicros;
    String priceCurrencyCode;
    String subscriptionPeriod;
    String freeTrialPeriod;
    String introductoryPrice;
    long introductoryPriceAmountMicros;
    String introductoryPricePeriod;
    Integer introductoryPriceCycles;

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
                .priceAmountMicros(getFieldAsLongOrZero(obj, FLD_PRICE_AMT_MICROS))
                .priceCurrencyCode(getFieldAsStringOrNull(obj, FLD_PRICE_CURR_CODE))
                .subscriptionPeriod(getFieldAsStringOrNull(obj, FLD_SUBSCRIPTION_PERIOD))
                .freeTrialPeriod(getFieldAsStringOrNull(obj, FLD_FREE_TRIAL_PERIOD))
                .introductoryPrice(getFieldAsStringOrNull(obj, FLD_INTRODUCTORY_PRICE))
                .introductoryPriceAmountMicros(getFieldAsLongOrZero(obj, FLD_INTRODUCTORY_PRICE_AMT_MICROS))
                .introductoryPricePeriod(getFieldAsStringOrNull(obj, FLD_INTRODUCTORY_PRICE_PERIOD))
                .introductoryPriceCycles(getFieldAsIntOrZero(obj, FLD_INTRODUCTORY_PRICE_CYCLES))
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
        addToObjIfNotNull(FLD_SUBSCRIPTION_PERIOD, googleProductResponse.subscriptionPeriod(), obj);
        addToObjIfNotNull(FLD_FREE_TRIAL_PERIOD, googleProductResponse.freeTrialPeriod(), obj);
        addToObjIfNotNull(FLD_INTRODUCTORY_PRICE, googleProductResponse.introductoryPrice(), obj);
        addToObj(FLD_INTRODUCTORY_PRICE_AMT_MICROS, googleProductResponse.introductoryPriceAmountMicros(), obj);
        addToObjIfNotNull(FLD_INTRODUCTORY_PRICE_PERIOD, googleProductResponse.introductoryPricePeriod(), obj);
        addToObj(FLD_INTRODUCTORY_PRICE_CYCLES, googleProductResponse.introductoryPriceCycles(), obj);
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

        public Builder priceAmountMicros(long priceAmountMicros) {
            builderObject.priceAmountMicros = priceAmountMicros;
            return this;
        }

        public Builder priceCurrencyCode(String priceCurrencyCode) {
            builderObject.priceCurrencyCode = priceCurrencyCode;
            return this;
        }

        public Builder subscriptionPeriod(String subscriptionPeriod) {
            builderObject.subscriptionPeriod = subscriptionPeriod;
            return this;
        }

        public Builder freeTrialPeriod(String freeTrialPeriod) {
            builderObject.freeTrialPeriod = freeTrialPeriod;
            return this;
        }

        public Builder introductoryPrice(String introductoryPrice) {
            builderObject.introductoryPrice = introductoryPrice;
            return this;
        }

        public Builder introductoryPriceAmountMicros(long introductoryPriceAmountMicros) {
            builderObject.introductoryPriceAmountMicros = introductoryPriceAmountMicros;
            return this;
        }

        public Builder introductoryPricePeriod(String introductoryPricePeriod) {
            builderObject.introductoryPricePeriod = introductoryPricePeriod;
            return this;
        }

        public Builder introductoryPriceCycles(Integer introductoryPriceCycles) {
            builderObject.introductoryPriceCycles = introductoryPriceCycles;
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

    public long priceAmountMicros() {
        return priceAmountMicros;
    }

    public String priceCurrencyCode() {
        return priceCurrencyCode;
    }

    public String subscriptionPeriod() {
        return subscriptionPeriod;
    }

    public String freeTrialPeriod() {
        return freeTrialPeriod;
    }

    public String introductoryPrice() {
        return introductoryPrice;
    }

    public long introductoryPriceAmountMicros() {
        return introductoryPriceAmountMicros;
    }

    public String introductoryPricePeriod() {
        return introductoryPricePeriod;
    }

    public Integer introductoryPriceCycles() {
        return introductoryPriceCycles;
    }
}
