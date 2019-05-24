package com.nytimes.android.external.registerlib

import android.text.TextUtils
import android.util.Log
import com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsStringOrNull
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * class structure representing INAPP_PURCHASE_DATA.
 */
class InAppPurchaseData {

    internal var orderId: String? = null
    internal var packageName: String? = null
    internal var productId: String? = null
    internal var purchaseTime: String? = null
    internal var purchaseState: String? = null
    internal var developerPayload: String? = null
    internal var purchaseToken: String? = null

    fun orderId(): String? {
        return orderId
    }

    fun packageName(): String? {
        return packageName
    }

    fun productId(): String? {
        return productId
    }

    fun purchaseTime(): String? {
        return purchaseTime
    }

    fun purchaseState(): String? {
        return purchaseState
    }

    fun developerPayload(): String? {
        return developerPayload
    }

    fun purchaseToken(): String? {
        return purchaseToken
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null) {
            return false
        }
        if (o !is InAppPurchaseData) {
            return false
        }
        val other = o as InAppPurchaseData?
        return TextUtils.equals(orderId, other!!.orderId) &&
                TextUtils.equals(packageName, other.packageName) &&
                TextUtils.equals(productId, other.productId) &&
                TextUtils.equals(purchaseTime, other.purchaseTime) &&
                TextUtils.equals(purchaseState, other.purchaseState) &&
                TextUtils.equals(developerPayload, other.developerPayload) &&
                TextUtils.equals(purchaseToken, other.purchaseToken)
    }

    override fun hashCode(): Int {
        return Objects.hash(orderId, packageName, productId, purchaseTime, purchaseState, developerPayload,
                purchaseToken)
    }

    class Builder {
        private val inAppPurchaseData: InAppPurchaseData = InAppPurchaseData()

        fun orderId(orderId: String?): Builder {
            inAppPurchaseData.orderId = orderId
            return this
        }

        fun packageName(packageName: String?): Builder {
            inAppPurchaseData.packageName = packageName
            return this
        }

        fun productId(productId: String?): Builder {
            inAppPurchaseData.productId = productId
            return this
        }

        fun purchaseTime(purchaseTime: String?): Builder {
            inAppPurchaseData.purchaseTime = purchaseTime
            return this
        }

        fun purchaseState(purchaseState: String?): Builder {
            inAppPurchaseData.purchaseState = purchaseState
            return this
        }

        fun developerPayload(developerPayload: String?): Builder {
            inAppPurchaseData.developerPayload = developerPayload
            return this
        }

        fun purchaseToken(purchaseToken: String?): Builder {
            inAppPurchaseData.purchaseToken = purchaseToken
            return this
        }

        fun build(): InAppPurchaseData {
            return inAppPurchaseData
        }
    }

    companion object {
        private val TAG = InAppPurchaseData::class.java.simpleName
        private const val FLD_ORDER_ID = "orderId"
        private const val FLD_PACKAGE = "package"
        private const val FLD_PRODUCT_ID = "productId"
        private const val FLD_PURCHASE_TIME = "purchaseTime"
        private const val FLD_PURCHASE_STATE = "purchaseState"
        private const val FLD_DEV_PAYLOAD = "developerPayload"
        private const val FLD_PURCHASE_TOKEN = "purchaseToken"

        @JvmStatic
        fun fromJson(json: String): InAppPurchaseData {
            var obj: JSONObject? = null
            try {
                obj = JSONObject(json)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error creating json", exc)
            }

            return Builder()
                    .orderId(getFieldAsStringOrNull(obj, FLD_ORDER_ID))
                    .packageName(getFieldAsStringOrNull(obj, FLD_PACKAGE))
                    .productId(getFieldAsStringOrNull(obj, FLD_PRODUCT_ID))
                    .purchaseTime(getFieldAsStringOrNull(obj, FLD_PURCHASE_TIME))
                    .purchaseState(getFieldAsStringOrNull(obj, FLD_PURCHASE_STATE))
                    .developerPayload(getFieldAsStringOrNull(obj, FLD_DEV_PAYLOAD))
                    .purchaseToken(getFieldAsStringOrNull(obj, FLD_PURCHASE_TOKEN))
                    .build()
        }

        @JvmStatic
        fun toJson(inAppPurchaseData: InAppPurchaseData): String {
            val obj = JSONObject()
            addToObjIfNotNull(FLD_ORDER_ID, inAppPurchaseData.orderId(), obj)
            addToObjIfNotNull(FLD_PACKAGE, inAppPurchaseData.packageName(), obj)
            addToObjIfNotNull(FLD_PRODUCT_ID, inAppPurchaseData.productId(), obj)
            addToObjIfNotNull(FLD_PURCHASE_TIME, inAppPurchaseData.purchaseTime(), obj)
            addToObjIfNotNull(FLD_PURCHASE_STATE, inAppPurchaseData.purchaseState(), obj)
            addToObjIfNotNull(FLD_DEV_PAYLOAD, inAppPurchaseData.developerPayload(), obj)
            addToObjIfNotNull(FLD_PURCHASE_TOKEN, inAppPurchaseData.purchaseToken(), obj)
            return obj.toString()
        }
    }
}
