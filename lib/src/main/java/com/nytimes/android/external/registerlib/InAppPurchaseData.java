package com.nytimes.android.external.registerlib;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull;
import static com.nytimes.android.external.registerlib.JsonHelper.getFieldAsStringOrNull;

/**
 * class structure representing INAPP_PURCHASE_DATA.
 */
public class InAppPurchaseData {
    private static final String TAG = InAppPurchaseData.class.getSimpleName();
    private static final String FLD_ORDER_ID = "orderId";
    private static final String FLD_PACKAGE = "package";
    private static final String FLD_PRODUCT_ID = "productId";
    private static final String FLD_PURCHASE_TIME = "purchaseTime";
    private static final String FLD_PURCHASE_STATE = "purchaseState";
    private static final String FLD_DEV_PAYLOAD = "developerPayload";
    private static final String FLD_PURCHASE_TOKEN = "purchaseToken";

    String orderId;
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

    public static InAppPurchaseData fromJson(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
        } catch (JSONException exc) {
            Log.e(TAG, "Error creating json", exc);
        }
        return new Builder()
                .orderId(getFieldAsStringOrNull(obj, FLD_ORDER_ID))
                .packageName(getFieldAsStringOrNull(obj, FLD_PACKAGE))
                .productId(getFieldAsStringOrNull(obj, FLD_PRODUCT_ID))
                .purchaseTime(getFieldAsStringOrNull(obj, FLD_PURCHASE_TIME))
                .purchaseState(getFieldAsStringOrNull(obj, FLD_PURCHASE_STATE))
                .developerPayload(getFieldAsStringOrNull(obj, FLD_DEV_PAYLOAD))
                .purchaseToken(getFieldAsStringOrNull(obj, FLD_PURCHASE_TOKEN))
                .build();
    }

    public static String toJson(InAppPurchaseData inAppPurchaseData) {
        JSONObject obj = new JSONObject();
        addToObjIfNotNull(FLD_ORDER_ID, inAppPurchaseData.orderId(), obj);
        addToObjIfNotNull(FLD_PACKAGE, inAppPurchaseData.packageName(), obj);
        addToObjIfNotNull(FLD_PRODUCT_ID, inAppPurchaseData.productId(), obj);
        addToObjIfNotNull(FLD_PURCHASE_TIME, inAppPurchaseData.purchaseTime(), obj);
        addToObjIfNotNull(FLD_PURCHASE_STATE, inAppPurchaseData.purchaseState(), obj);
        addToObjIfNotNull(FLD_DEV_PAYLOAD, inAppPurchaseData.developerPayload(), obj);
        addToObjIfNotNull(FLD_PURCHASE_TOKEN, inAppPurchaseData.purchaseToken(), obj);
        return obj.toString();
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
