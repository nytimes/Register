package com.nytimes.android.external.register

class PurchasesLists {
    var purchaseItemList: MutableList<String> = ArrayList()
    var purchaseDataList: MutableList<String> = ArrayList()
    var dataSignatureList: MutableList<String> = ArrayList()
    var continuationToken: String? = null
}
