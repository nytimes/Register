package com.nytimes.android.external.register.buy

import com.nytimes.android.external.registerlib.InAppPurchaseData

data class PurchaseResult(
        val successful: Boolean,
        val inAppPurchaseData: InAppPurchaseData
)