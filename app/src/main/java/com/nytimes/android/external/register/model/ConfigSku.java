package com.nytimes.android.external.register.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public abstract class ConfigSku {

    public abstract String type();
    public abstract String price();
    public abstract String title();
    public abstract String description();

    @Value.Default
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public int introductoryPriceCycles() {
        return 0;
    }

    @Nullable
    public abstract String freeTrialPeriod();
    @SerializedName("package")
    public abstract String packageName();
    @Nullable
    public abstract String introductoryPrice();
    @Nullable
    public abstract String introductoryPricePeriod();
    @Nullable
    public abstract String subscriptionPeriod();
}
