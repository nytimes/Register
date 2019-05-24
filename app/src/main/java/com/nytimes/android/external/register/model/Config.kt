package com.nytimes.android.external.register.model

data class Config(
        val skus: Map<String, ConfigSku>,
        val users: List<String>
)