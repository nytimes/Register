package com.nytimes.android.external.registerlib

import android.util.Log
import com.nytimes.android.external.registerlib.JsonHelper.addToObj
import com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsIntOrZero
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsLongOrZero
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsStringOrNull
import org.json.JSONException
import org.json.JSONObject

class GoogleProductResponse {

    var productId: String? = null
    var itemType: String? = null
    var price: String? = null
    var title: String? = null
    var description: String? = null
    var priceAmountMicros: Long = 0
    var priceCurrencyCode: String? = null
    var subscriptionPeriod: String? = null
    var freeTrialPeriod: String? = null
    var introductoryPrice: String? = null
    var introductoryPriceAmountMicros: Long = 0
    var introductoryPricePeriod: String? = null
    var introductoryPriceCycles: Int = 0

    class Builder {
        var builderObject = GoogleProductResponse()

        fun productId(productId: String?): Builder {
            builderObject.productId = productId
            return this
        }

        fun itemType(itemType: String?): Builder {
            builderObject.itemType = itemType
            return this
        }

        fun price(price: String?): Builder {
            builderObject.price = price
            return this
        }

        fun title(title: String?): Builder {
            builderObject.title = title
            return this
        }

        fun description(description: String?): Builder {
            builderObject.description = description
            return this
        }

        fun priceAmountMicros(priceAmountMicros: Long): Builder {
            builderObject.priceAmountMicros = priceAmountMicros
            return this
        }

        fun priceCurrencyCode(priceCurrencyCode: String?): Builder {
            builderObject.priceCurrencyCode = priceCurrencyCode
            return this
        }

        fun subscriptionPeriod(subscriptionPeriod: String?): Builder {
            builderObject.subscriptionPeriod = subscriptionPeriod
            return this
        }

        fun freeTrialPeriod(freeTrialPeriod: String?): Builder {
            builderObject.freeTrialPeriod = freeTrialPeriod
            return this
        }

        fun introductoryPrice(introductoryPrice: String?): Builder {
            builderObject.introductoryPrice = introductoryPrice
            return this
        }

        fun introductoryPriceAmountMicros(introductoryPriceAmountMicros: Long): Builder {
            builderObject.introductoryPriceAmountMicros = introductoryPriceAmountMicros
            return this
        }

        fun introductoryPricePeriod(introductoryPricePeriod: String?): Builder {
            builderObject.introductoryPricePeriod = introductoryPricePeriod
            return this
        }

        fun introductoryPriceCycles(introductoryPriceCycles: Int): Builder {
            builderObject.introductoryPriceCycles = introductoryPriceCycles
            return this
        }

        fun build(): GoogleProductResponse {
            return builderObject
        }
    }

    fun productId(): String? {
        return productId
    }

    fun itemType(): String? {
        return itemType
    }


    fun price(): String? {
        return price
    }

    fun title(): String? {
        return title
    }

    fun description(): String? {
        return description
    }

    fun priceAmountMicros(): Long {
        return priceAmountMicros
    }

    fun priceCurrencyCode(): String? {
        return priceCurrencyCode
    }

    fun subscriptionPeriod(): String? {
        return subscriptionPeriod
    }

    fun freeTrialPeriod(): String? {
        return freeTrialPeriod
    }

    fun introductoryPrice(): String? {
        return introductoryPrice
    }

    fun introductoryPriceAmountMicros(): Long {
        return introductoryPriceAmountMicros
    }

    fun introductoryPricePeriod(): String? {
        return introductoryPricePeriod
    }

    fun introductoryPriceCycles(): Int {
        return introductoryPriceCycles
    }

    companion object {
        private val TAG = GoogleProductResponse::class.java.simpleName
        private const val FLD_PRODUCT_ID = "productId"
        private const val FLD_ITEM_TYPE = "type"
        private const val FLD_PRICE = "price"
        private const val FLD_TITLE = "title"
        private const val FLD_DESCRIPTION = "description"
        private const val FLD_PRICE_AMT_MICROS = "price_amount_micros"
        private const val FLD_PRICE_CURR_CODE = "price_currency_code"
        private const val FLD_SUBSCRIPTION_PERIOD = "subscriptionPeriod"
        private const val FLD_FREE_TRIAL_PERIOD = "freeTrialPeriod"
        private const val FLD_INTRODUCTORY_PRICE = "introductoryPrice"
        private const val FLD_INTRODUCTORY_PRICE_AMT_MICROS = "introductoryPriceAmountMicros"
        private const val FLD_INTRODUCTORY_PRICE_PERIOD = "introductoryPricePeriod"
        private const val FLD_INTRODUCTORY_PRICE_CYCLES = "introductoryPriceCycles"

        @JvmStatic
        fun fromJson(json: String): GoogleProductResponse {
            var obj: JSONObject? = null
            try {
                obj = JSONObject(json)
            } catch (exc: JSONException) {
                Log.e(TAG, "Error creating json", exc)
            }

            return GoogleProductResponse.Builder()
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
                    .build()
        }

        @JvmStatic
        fun toJson(googleProductResponse: GoogleProductResponse): String {
            val obj = JSONObject()
            addToObjIfNotNull(FLD_PRODUCT_ID, googleProductResponse.productId(), obj)
            addToObjIfNotNull(FLD_ITEM_TYPE, googleProductResponse.itemType(), obj)
            addToObjIfNotNull(FLD_PRICE, googleProductResponse.price(), obj)
            addToObjIfNotNull(FLD_TITLE, googleProductResponse.title(), obj)
            addToObjIfNotNull(FLD_DESCRIPTION, googleProductResponse.description(), obj)
            addToObj(FLD_PRICE_AMT_MICROS, googleProductResponse.priceAmountMicros(), obj)
            addToObjIfNotNull(FLD_PRICE_CURR_CODE, googleProductResponse.priceCurrencyCode(), obj)
            addToObjIfNotNull(FLD_SUBSCRIPTION_PERIOD, googleProductResponse.subscriptionPeriod(), obj)
            addToObjIfNotNull(FLD_FREE_TRIAL_PERIOD, googleProductResponse.freeTrialPeriod(), obj)
            addToObjIfNotNull(FLD_INTRODUCTORY_PRICE, googleProductResponse.introductoryPrice(), obj)
            addToObj(FLD_INTRODUCTORY_PRICE_AMT_MICROS, googleProductResponse.introductoryPriceAmountMicros(), obj)
            addToObjIfNotNull(FLD_INTRODUCTORY_PRICE_PERIOD, googleProductResponse.introductoryPricePeriod(), obj)
            addToObj(FLD_INTRODUCTORY_PRICE_CYCLES, googleProductResponse.introductoryPriceCycles(), obj)
            return obj.toString()
        }
    }
}
