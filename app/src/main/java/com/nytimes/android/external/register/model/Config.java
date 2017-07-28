package com.nytimes.android.external.register.model;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@Gson.TypeAdapters
public interface Config {
    Map<String, ConfigSku> skus();
    List<String> users();
}
