package com.nytimes.android.external.register.model

import com.google.gson.annotations.SerializedName

data class ConfigSku(
        val type: String,
        val price: String,
        val title: String,
        val description: String,
        @SerializedName("package")
        val packageName: String,
        val introductoryPriceCycles: Int = 0,
        val freeTrialPeriod: String? = null,
        val introductoryPrice: String? = null,
        val introductoryPricePeriod: String? = null,
        val subscriptionPeriod: String? = null
)