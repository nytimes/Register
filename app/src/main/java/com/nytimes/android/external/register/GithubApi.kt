package com.nytimes.android.external.register

import com.nytimes.android.external.register.model.Repository

import io.reactivex.Observable
import retrofit2.http.GET

interface GithubApi {
    @get:GET(BuildConfig.GITHUB_ENDPOINT)
    val playBillingRepository: Observable<Repository>
}
