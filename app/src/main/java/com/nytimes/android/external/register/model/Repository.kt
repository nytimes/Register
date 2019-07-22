package com.nytimes.android.external.register.model

import java.util.*

data class Repository(
    val name: String,
    val fullName: String,
    val description: String,
    val pushedAt: Date,
    val stargazersCount: Int,
    val forksCount: Int,
    val language: String,
    val htmlUrl: String
)
