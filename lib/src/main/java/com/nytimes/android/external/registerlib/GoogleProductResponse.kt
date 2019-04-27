package com.nytimes.android.external.registerlib

import android.util.Log
import com.nytimes.android.external.registerlib.JsonHelper.addToObj
import com.nytimes.android.external.registerlib.JsonHelper.addToObjIfNotNull
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsIntOrNull
import com.nytimes.android.external.registerlib.JsonHelper.getFieldAsStringOrNull
import org.json.JSONException
import org.json.JSONObject

class GoogleProductResponse {

    var productId: String? = null
    var itemType: String? = null
    var price: String? = null
    var title: String? = null
    var description: String? = null
    var priceAmountMicros: Int = 0
    var priceCurrencyCode: String? = null

    class Builder {
        private var builderObject = GoogleProductResponse()

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

        fun priceAmountMicros(priceAmountMicros: Int): Builder {
            builderObject.priceAmountMicros = priceAmountMicros
            return this
        }

        fun priceCurrencyCode(priceCurrencyCode: String?): Builder {
            builderObject.priceCurrencyCode = priceCurrencyCode
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

    fun priceAmountMicros(): Int {
        return priceAmountMicros
    }

    fun priceCurrencyCode(): String? {
        return priceCurrencyCode
    }

    companion object {
        private val TAG = GoogleProductResponse::class.java.simpleName
        private val FLD_PRODUCT_ID = "productId"
        private val FLD_ITEM_TYPE = "type"
        private val FLD_PRICE = "price"
        private val FLD_TITLE = "title"
        private val FLD_DESCRIPTION = "description"
        private val FLD_PRICE_AMT_MICROS = "price_amount_micros"
        private val FLD_PRICE_CURR_CODE = "price_currency_code"

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
                    .priceAmountMicros(getFieldAsIntOrNull(obj, FLD_PRICE_AMT_MICROS)!!)
                    .priceCurrencyCode(getFieldAsStringOrNull(obj, FLD_PRICE_CURR_CODE))
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
            return obj.toString()
        }
    }
}
