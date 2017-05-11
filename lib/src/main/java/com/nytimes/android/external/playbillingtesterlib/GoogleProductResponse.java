package com.nytimes.android.external.playbillingtesterlib;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class GoogleProductResponse {
    String productId;
    String itemType;
    String price;
    String title;
    String description;
    @SerializedName("price_amount_micros")
    long priceAmountMicros;
    @SerializedName("price_currency_code")
    String priceCurrencyCode;

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

    public long priceAmountMicros() {
        return priceAmountMicros;
    }

    public String priceCurrencyCode() {
        return priceCurrencyCode;
    }
}
