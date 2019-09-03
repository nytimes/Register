package com.nytimes.android.external.register

import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.registerlib.GoogleUtil
import javax.inject.Inject

class BillingServiceStubImpl @Inject
constructor(private val apiOverrides: APIOverrides,
            private val buyIntentBundleBuilder: BuyIntentBundleBuilder,
            private val skuDetailsBundleBuilder: SkuDetailsBundleBuilder,
            private val purchasesBundleBuilder: PurchasesBundleBuilder,
            private val consumePurchaseResponse: ConsumePurchaseResponse,
            private val buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder) : IInAppBillingService.Stub() {

    override fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
        var response = apiOverrides.isBillingSupportedResponse
        if (response == APIOverrides.RESULT_DEFAULT) {
            response = if (apiVersion <= GoogleUtil.BILLING_API_VERSION)
                GoogleUtil.RESULT_OK
            else
                GoogleUtil.RESULT_BILLING_UNAVAILABLE
        }
        return response
    }

    override fun getSkuDetails(apiVersion: Int, packageName: String, type: String, skusBundle: Bundle): Bundle {
        return skuDetailsBundleBuilder
                .newBuilder()
                .skus(skusBundle.getStringArrayList(GoogleUtil.ITEM_ID_LIST), type)
                .build()
    }

    override fun getBuyIntent(apiVersion: Int, packageName: String, sku: String, type: String,
                              developerPayload: String?): Bundle {
        return buyIntentBundleBuilder
                .newBuilder()
                .packageName(packageName)
                .sku(sku)
                .type(type)
                .developerPayload(developerPayload)
                .build()
    }

    override fun getPurchases(apiVersion: Int, packageName: String, type: String, continuationToken: String?): Bundle {
        return purchasesBundleBuilder
                .newBuilder()
                .type(type)
                .continuationToken(continuationToken)
                .build()
    }

    override fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        return consumePurchaseResponse.consumePurchase(apiVersion, packageName, purchaseToken)
    }

    @Throws(RemoteException::class)
    override fun stub(apiVersion: Int, packageName: String, type: String): Int {
        // We are not using this call
        return 0
    }

    @Throws(RemoteException::class)
    override fun getBuyIntentToReplaceSkus(apiVersion: Int, packageName: String, oldSkus: List<String>,
                                           newSku: String, type: String,
                                           developerPayload: String): Bundle {
        return buyIntentToReplaceSkusBundleBuilder
                .newBuilder()
                .packageName(packageName)
                .oldSkus(oldSkus)
                .newSku(newSku)
                .type(type)
                .developerPayload(developerPayload)
                .build()
    }

    override fun asBinder(): IBinder {
        return this
    }
}
