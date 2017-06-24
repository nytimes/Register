package com.nytimes.android.external.playbillingtester.model;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@Gson.TypeAdapters(fieldNamingStrategy = true)
public interface Repository {
    String name();
    String fullName();
    String description();
    Date pushedAt();
    int stargazersCount();
    int forksCount();
    String language();
    String htmlUrl();
}
