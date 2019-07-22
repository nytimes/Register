package com.nytimes.android.external.register.bundle

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.GoogleUtil.RESULT_OK
import javax.inject.Inject

class ConsumePurchaseResponse @Inject
constructor(apiOverrides: APIOverrides, private val purchases: Purchases) : BaseResponse(apiOverrides) {

    override fun rawResponseCode(): Int {
        return apiOverrides.consumePurchaseResponse
    }

    fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        val responseCode = responseCode()
        val inAppPurchaseItems: List<String>
        val subscriptionsItems: List<String>
        if (responseCode == RESULT_OK) {
            subscriptionsItems = purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null)
                    .purchaseDataList
            if (subscriptionsItems.contains(purchaseToken)) {
                return GoogleUtil.RESULT_ERROR
            }
            inAppPurchaseItems = purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null).purchaseDataList
            if (inAppPurchaseItems.contains(purchaseToken)) {
                if (!purchases.removePurchase(purchaseToken, GoogleUtil.BILLING_TYPE_IAP)) {
                    return GoogleUtil.RESULT_ERROR
                }
            } else {
                return GoogleUtil.RESULT_ITEM_NOT_OWNED
            }
        }
        return responseCode()
    }

}
