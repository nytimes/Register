package com.nytimes.android.external.playbillingtester;


import com.nytimes.android.external.playbillingtester.model.Repository;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface GithubApi {
    String API_BASE_URL = "https://api.github.com";
    String API_ENDPOINT = "/repos/NYTimes/Register";

    @GET(API_ENDPOINT)
    Observable<Repository> getPlayBillingRepository();
}