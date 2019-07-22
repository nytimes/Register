package com.nytimes.android.external.register

import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.util.Log
import com.google.common.collect.Collections2
import com.google.common.collect.ImmutableSet
import com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_IAP
import com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_SUBSCRIPTION
import com.nytimes.android.external.registerlib.InAppPurchaseData
import java.security.InvalidKeyException
import java.security.SignatureException
import java.util.*
import javax.inject.Inject

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

class Purchases @Inject
constructor(private val sharedPreferences: SharedPreferences, private val signer: Signer) {

    fun getPurchasesLists(type: String, continuationToken: String?): PurchasesLists {
        val item = PurchasesLists()
        val data = ArrayList(getInAppPurchaseData(type))
        val first = getFirst(continuationToken)
        val limit = Math.min(first + PAGE_LIMIT, data.size)
        for (i in first until limit) {
            val jsonData = InAppPurchaseData.toJson(data[i])
            item.purchaseItemList.add(data[i].productId()!!)
            item.purchaseDataList.add(jsonData)
            var signedData = ""
            try {
                signedData = signer.signData(jsonData)
            } catch (exception: InvalidKeyException) {
                Log.e("Purchases", "Exception signing purchase data", exception)
            } catch (exception: SignatureException) {
                Log.e("Purchases", "Exception signing purchase data", exception)
            }

            item.dataSignatureList.add(signedData)
        }
        if (limit < data.size) {
            item.continuationToken = String.format("%d", limit)
        }
        return item
    }

    fun addPurchase(inAppPurchaseDataStr: String, itemType: String): Boolean {
        val items = getPurchases(itemType)
        if (items!!.contains(inAppPurchaseDataStr)) {
            return false
        } else {
            val toAdd = ImmutableSet.Builder<String>().addAll(items).add(inAppPurchaseDataStr).build()
            sharedPreferences.edit().putStringSet(itemType, toAdd).apply()
            return true
        }
    }

    fun removePurchase(inAppPurchaseDataStr: String, itemType: String): Boolean {
        val items = getPurchases(itemType)
        val removed = items!!.remove(inAppPurchaseDataStr)
        if (removed) {
            sharedPreferences.edit().putStringSet(itemType, items).apply()
        }
        return removed
    }

    fun replacePurchase(newInAppPurchaseDataStr: String, replacedSkus: List<String>): Boolean {
        val purchasedItems = getPurchases(BILLING_TYPE_SUBSCRIPTION)
        val purchasedSkus = getPurchasedSkus(BILLING_TYPE_SUBSCRIPTION)
        val builder = ImmutableSet.builder<String>()
        if (purchasedSkus.containsAll(replacedSkus) && !purchasedItems!!.contains(newInAppPurchaseDataStr)) {
            val finalSet = builder
                    .addAll(getPurchasesExceptForSkus(BILLING_TYPE_SUBSCRIPTION, ImmutableSet.copyOf(replacedSkus)))
                    .add(newInAppPurchaseDataStr)
                    .build()
            sharedPreferences.edit().putStringSet(BILLING_TYPE_SUBSCRIPTION,
                    finalSet).commit()
            return true
        }
        return false
    }

    fun getInAppPurchaseData(itemType: String): Set<InAppPurchaseData> {
        val ret = LinkedHashSet<InAppPurchaseData>()
        for (json in getPurchases(itemType)!!) {
            ret.add(InAppPurchaseData.fromJson(json))
        }
        return ret
    }

    fun getReceiptsForSkus(skus: Set<String>, itemType: String): Set<String> {
        val builder = ImmutableSet.builder<String>()
        for (json in getPurchases(itemType)!!) {
            val inAppPurchaseData = InAppPurchaseData.fromJson(json)
            if (skus.contains(inAppPurchaseData.productId())) {
                builder.add(inAppPurchaseData.purchaseToken()!!)
            }
        }
        return builder.build()
    }

    fun purgePurchases() {
        sharedPreferences.edit().remove(BILLING_TYPE_IAP).remove(BILLING_TYPE_SUBSCRIPTION).apply()
    }

    private fun getPurchases(itemType: String): MutableSet<String>? {
        return sharedPreferences.getStringSet(itemType, ImmutableSet.of())
    }

    private fun getPurchasesExceptForSkus(itemType: String, skuFilter: Set<String>): Set<String> {
        return ImmutableSet.copyOf(Collections2.filter(getPurchases(itemType)!!
        ) { json -> !skuFilter.contains(InAppPurchaseData.fromJson(json!!).productId()) })
    }

    private fun getPurchasedSkus(itemType: String): Set<String> {
        val builder = ImmutableSet.builder<String>()
        for (inAppPurchaseDataStr in getPurchases(itemType)!!) {
            builder.add(InAppPurchaseData.fromJson(inAppPurchaseDataStr).productId()!!)
        }
        return builder.build()
    }

    private fun getFirst(continuationToken: String?): Int {
        return try {
            Integer.parseInt(continuationToken)
        } catch (exception: NumberFormatException) {
            0
        }
    }

    companion object {
        @VisibleForTesting
        const val PAGE_LIMIT = 100 // not sure what this limit actually is
    }

}
