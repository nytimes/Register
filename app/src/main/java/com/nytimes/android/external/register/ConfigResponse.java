package com.nytimes.android.external.register;


import org.immutables.value.Value;

@Value.Immutable
public interface ConfigResponse {
    String responseId();
    String responseName();
    int responseCode();
}
