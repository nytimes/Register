package com.nytimes.android.external.register.bundle

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.BuyActivity
import com.nytimes.android.external.registerlib.GoogleUtil

import javax.inject.Inject

class BuyIntentBundleBuilder @Inject
constructor(protected val application: Application, apiOverrides: APIOverrides) : BaseBundleBuilder(apiOverrides) {

    protected lateinit var intent: Intent

    fun newBuilder(): BuyIntentBundleBuilder {
        bundle = Bundle()
        intent = Intent(application, BuyActivity::class.java)
        return this
    }

    fun packageName(packageName: String): BuyIntentBundleBuilder {
        intent.putExtra(EX_PACKAGE_NAME, packageName)
        return this
    }

    fun sku(sku: String): BuyIntentBundleBuilder {
        intent.putExtra(EX_SKU, sku)
        return this
    }

    fun type(type: String): BuyIntentBundleBuilder {
        intent.putExtra(EX_ITEM_TYPE, type)
        return this
    }

    fun developerPayload(developerPayload: String?): BuyIntentBundleBuilder {
        developerPayload?.let { intent.putExtra(EX_DEVELOPER_PAYLOAD, it) }
        return this
    }

    fun build(): Bundle {
        val responseCode = responseCode()
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode)

        if (responseCode == GoogleUtil.RESULT_OK) {
            val pendingIntent = PendingIntent.getActivity(application, 0, intent,
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
        const val EX_SKU = "sku"
        const val EX_ITEM_TYPE = "type"
        const val EX_DEVELOPER_PAYLOAD = "developerPayload"
    }
}
