package com.nytimes.android.external.register.buy

data class PurchaseData(
        var sku: String? = null,
        var newSku: String? = null,
        var oldSkus: List<String>? = null,
        var itemtype: String? = null,
        var developerPayload: String? = null,
        var isReplace: Boolean = false
)