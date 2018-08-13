package com.nytimes.android.external.register.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface ConfigSku {
    String itemType();
    String price();
    String title();
    String description();
    @SerializedName("package")
    String packageName();
    @Nullable
    String introductoryPrice();
}
