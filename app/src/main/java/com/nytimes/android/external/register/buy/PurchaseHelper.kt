package com.nytimes.android.external.register.buy

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.register.Purchases
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_SUBSCRIPTION
import com.nytimes.android.external.registerlib.InAppPurchaseData
import java.util.Locale
import javax.inject.Inject

class PurchaseHelper @Inject constructor(
    @JvmField private val config: Config?,
    private val apiOverrides: APIOverrides,
    private val purchases: Purchases
) {

    fun onBuy(purchaseData: PurchaseData, currentTimeMillis: Long): PurchaseResult {
        val newReceipt = String.format(Locale.getDefault(), BuyFragment.RECEIPT_FMT,
            apiOverrides.usersResponse, currentTimeMillis)
        val skuToPurchase = if (purchaseData.isReplace) purchaseData.newSku else purchaseData.sku
        val inAppPurchaseData = InAppPurchaseData.Builder()
            .orderId(currentTimeMillis.toString())
            .packageName(config!!.skus[skuToPurchase]!!.packageName)
            .productId(skuToPurchase)
            .purchaseTime(currentTimeMillis.toString())
            .developerPayload(purchaseData.developerPayload)
            .purchaseToken(newReceipt)
            .isAutoRenewing(config.skus[skuToPurchase]!!.type == BILLING_TYPE_SUBSCRIPTION)
            .purchaseState("0")
            .build()
        val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
        val result: Boolean = if (purchaseData.isReplace) {
            purchases.replacePurchase(inAppPurchaseDataStr, purchaseData.oldSkus!!)
        } else {
            purchases.addPurchase(inAppPurchaseDataStr, purchaseData.itemtype!!)
        }
        return PurchaseResult(result, inAppPurchaseData)
    }
}