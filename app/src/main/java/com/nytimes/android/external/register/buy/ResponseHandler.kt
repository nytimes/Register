package com.nytimes.android.external.register.buy

import android.os.Bundle
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleUtil
import java.util.*
import javax.inject.Inject

class ResponseHandler @Inject constructor(
        @JvmField private val config: Config?
) {

    fun getResponseContent(buyResponse: Int, purchaseData: PurchaseData): Bundle {
        val bundle = Bundle()
        when (buyResponse) {
            GoogleUtil.RESULT_OK -> if (config != null) {
                val configSku = config.skus[if (purchaseData.isReplace) purchaseData.newSku else purchaseData.sku]!!

                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, configSku.title)
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY, configSku.description)
                bundle.putString(BuyFragment.RESPONSE_EXTRA_PRICE, String.format(BuyFragment.PRICE_FMT, configSku.price))

                if (purchaseData.isReplace) {
                    val oldSkuTitles = ArrayList<String>()
                    for (oldSku in purchaseData.oldSkus!!) {
                        oldSkuTitles.add(config.skus[oldSku]!!.title + "\n")
                    }
                    bundle.putStringArrayList(BuyFragment.RESPONSE_EXTRA_REPLACE_OLD_SKU, oldSkuTitles)
                }
            }
            GoogleUtil.RESULT_ITEM_UNAVAILABLE -> {
                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, "Error")
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY, "Item not found")
            }
            GoogleUtil.RESULT_ITEM_ALREADY_OWNED -> {
                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, "Error")
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY, "Item already owned")
            }
            GoogleUtil.RESULT_ITEM_NOT_OWNED -> {
                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, "Error")
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY, "Item(s) to be replaced not owned.")
            }
            GoogleUtil.RESULT_DEVELOPER_ERROR, GoogleUtil.RESULT_ERROR -> {
                val title = "Error"
                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, title)
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY,
                        String.format(Locale.getDefault(), BuyFragment.ERROR_FMT, title, buyResponse))
            }
            else -> {
                val title = "Error"
                bundle.putString(BuyFragment.RESPONSE_EXTRA_TITLE, title)
                bundle.putString(BuyFragment.RESPONSE_EXTRA_SUMMARY, String.format(Locale.getDefault(), BuyFragment.ERROR_FMT, title, buyResponse))
            }
        }
        return bundle
    }
}