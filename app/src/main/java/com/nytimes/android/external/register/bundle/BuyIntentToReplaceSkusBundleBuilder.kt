package com.nytimes.android.external.register.bundle

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.BuyActivity
import com.nytimes.android.external.registerlib.GoogleUtil
import java.util.*
import javax.inject.Inject

class BuyIntentToReplaceSkusBundleBuilder @Inject
constructor(protected val context: Context, apiOverrides: APIOverrides) : BaseBundleBuilder(apiOverrides) {

    protected lateinit var intent: Intent

    fun newBuilder(): BuyIntentToReplaceSkusBundleBuilder {
        bundle = Bundle()
        intent = Intent(context, BuyActivity::class.java)
        return this
    }

    fun packageName(packageName: String): BuyIntentToReplaceSkusBundleBuilder {
        intent.putExtra(EX_PACKAGE_NAME, packageName)
        return this
    }

    fun newSku(newSku: String): BuyIntentToReplaceSkusBundleBuilder {
        intent.putExtra(EX_NEW_SKU, newSku)
        return this
    }

    fun oldSkus(oldSkus: List<String>): BuyIntentToReplaceSkusBundleBuilder {
        intent.putStringArrayListExtra(EX_OLD_SKUS, ArrayList(oldSkus))
        return this
    }

    fun type(type: String): BuyIntentToReplaceSkusBundleBuilder {
        intent.putExtra(EX_ITEM_TYPE, type)
        return this
    }

    fun developerPayload(developerPayload: String?): BuyIntentToReplaceSkusBundleBuilder {
        intent.putExtra(EX_DEVELOPER_PAYLOAD, developerPayload)
        return this
    }

    fun build(): Bundle {
        val responseCode = responseCode()
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode)

        if (responseCode == GoogleUtil.RESULT_OK) {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            bundle.putParcelable(GoogleUtil.BUY_INTENT, pendingIntent)
        }
        return bundle
    }

    override fun rawResponseCode(): Int {
        return apiOverrides.getBuyIntentResponse
    }

    companion object {
        const val EX_PACKAGE_NAME = "packageName"
        const val EX_ITEM_TYPE = "type"
        const val EX_OLD_SKUS = "oldSkus"
        const val EX_NEW_SKU = "newSku"
        const val EX_DEVELOPER_PAYLOAD = "developerPayload"
    }
}
