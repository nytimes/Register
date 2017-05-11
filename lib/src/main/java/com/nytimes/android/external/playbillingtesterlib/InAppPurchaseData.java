package com.nytimes.android.external.playbillingtesterlib;


import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Gson-friendly class structure representing INAPP_PURCHASE_DATA.
 */
public class InAppPurchaseData {

    String orderId;
    @SerializedName("package")
    String packageName;
    String productId;
    String purchaseTime;
    String purchaseState;
    String developerPayload;
    String purchaseToken;

    public String orderId() {
        return orderId;
    }

    public String packageName() {
        return packageName;
    }

    public String productId() {
        return productId;
    }

    public String purchaseTime() {
        return purchaseTime;
    }

    public String purchaseState() {
        return purchaseState;
    }

    public String developerPayload() {
        return developerPayload;
    }

    public String purchaseToken() {
        return purchaseToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof InAppPurchaseData)) {
            return false;
        }
        InAppPurchaseData other = (InAppPurchaseData) o;
        return TextUtils.equals(orderId, other.orderId) &&
                TextUtils.equals(packageName, other.packageName) &&
                TextUtils.equals(productId, other.productId) &&
                TextUtils.equals(purchaseTime, other.purchaseTime) &&
                TextUtils.equals(purchaseState, other.purchaseState) &&
                TextUtils.equals(developerPayload, other.developerPayload) &&
                TextUtils.equals(purchaseToken, other.purchaseToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, packageName, productId, purchaseTime, purchaseState, developerPayload,
                purchaseToken);
    }

    public static class Builder {
        private final InAppPurchaseData inAppPurchaseData;

        public Builder() {
            inAppPurchaseData = new InAppPurchaseData();
        }

        public Builder orderId(String orderId) {
            inAppPurchaseData.orderId = orderId;
            return this;
        }

        public Builder packageName(String packageName) {
            inAppPurchaseData.packageName = packageName;
            return this;
        }

        public Builder productId(String productId) {
            inAppPurchaseData.productId = productId;
            return this;
        }

        public Builder purchaseTime(String purchaseTime) {
            inAppPurchaseData.purchaseTime = purchaseTime;
            return this;
        }

        public Builder purchaseState(String purchaseState) {
            inAppPurchaseData.purchaseState = purchaseState;
            return this;
        }

        public Builder developerPayload(String developerPayload) {
            inAppPurchaseData.developerPayload = developerPayload;
            return this;
        }

        public Builder purchaseToken(String purchaseToken) {
            inAppPurchaseData.purchaseToken = purchaseToken;
            return this;
        }

        public InAppPurchaseData build() {
            return inAppPurchaseData;
        }
    }
}
