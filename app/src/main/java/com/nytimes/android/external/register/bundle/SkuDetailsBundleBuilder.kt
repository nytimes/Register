package com.nytimes.android.external.register.bundle

import android.os.Bundle
import com.google.common.base.Optional
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleProductResponse
import com.nytimes.android.external.registerlib.GoogleUtil
import java.util.*
import javax.inject.Inject

class SkuDetailsBundleBuilder @Inject
constructor(apiOverrides: APIOverrides, private val config: Optional<Config>) : BaseBundleBuilder(apiOverrides) {

    private var detailsList: MutableList<String>? = null

    fun newBuilder(): SkuDetailsBundleBuilder {
        bundle = Bundle()
        detailsList = ArrayList()
        return this
    }

    fun skus(skus: List<String>, type: String): SkuDetailsBundleBuilder {
        if (config.isPresent) {
            for (sku in skus) {
                sku(sku, type)
            }
        }
        return this
    }

    private fun sku(sku: String, type: String) {
        val configSku = config.get().skus[sku]
        if (configSku != null) {
            val builder = GoogleProductResponse.Builder()
                    .productId(sku)
                    .itemType(type)
                    .description(configSku.description)
                    .title(configSku.title)
                    .price("$" + configSku.price)
                    .priceAmountMicros((java.lang.Double.parseDouble(configSku.price) * 1000000).toInt().toLong())
                    .subscriptionPeriod(configSku.subscriptionPeriod)
                    .freeTrialPeriod(configSku.freeTrialPeriod)
                    .priceCurrencyCode("USD")

            if (configSku.introductoryPrice != null) {
                builder.introductoryPrice("$" + configSku.introductoryPrice)
                        .introductoryPriceAmountMicros((java.lang.Double.parseDouble(configSku
                                .introductoryPrice) * 1000000).toInt().toLong())
                        .introductoryPriceCycles(configSku.introductoryPriceCycles)
                        .introductoryPricePeriod(configSku.introductoryPricePeriod)
            }

            val googleProductResponse = builder.build()
            detailsList!!.add(GoogleProductResponse.toJson(googleProductResponse))
        }
    }

    fun build(): Bundle {
        var responseCode = responseCode()
        if (responseCode == APIOverrides.RESULT_DEFAULT) {
            responseCode = GoogleUtil.RESULT_OK
        }
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode)
        if (responseCode == GoogleUtil.RESULT_OK) {
            bundle.putStringArrayList(GoogleUtil.DETAILS_LIST, ArrayList(detailsList!!))
        }
        return bundle
    }

    override fun rawResponseCode(): Int {
        return apiOverrides.getSkuDetailsResponse
    }
}
