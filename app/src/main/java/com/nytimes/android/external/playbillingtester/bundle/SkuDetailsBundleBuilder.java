package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.APIOverrides;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ConfigSku;
import com.nytimes.android.external.playbillingtesterlib.GoogleProductResponse;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SkuDetailsBundleBuilder extends BaseBundleBuilder {

    private List<String> detailsList;
    private final Optional<Config> config;
    private final Gson gson;

    @Inject
    public SkuDetailsBundleBuilder(APIOverrides apiOverrides, Optional<Config> config, Gson gson) {
        super(apiOverrides);
        this.config = config;
        this.gson = gson;
    }

    public SkuDetailsBundleBuilder newBuilder() {
        bundle = new Bundle();
        detailsList = new ArrayList<>();
        return this;
    }

    public SkuDetailsBundleBuilder skus(List<String> skus, String type) {
        if (config.isPresent()) {
            for (String sku : skus) {
                sku(sku, type);
            }
        }
        return this;
    }

    private void sku(String sku, String type) {
        ConfigSku configSku = config.get().skus().get(sku);
        if (configSku != null) {
            GoogleProductResponse googleProductResponse = new GoogleProductResponse.Builder()
                    .productId(sku)
                    .itemType(type)
                    .description(configSku.description())
                    .title(configSku.title())
                    .price("$" + configSku.price())
                    .priceAmountMicros((int) (Double.parseDouble(configSku.price()) * 1000000))
                    .priceCurrencyCode("USD")
                    .build();
            detailsList.add(gson.toJson(googleProductResponse, GoogleProductResponse.class));
        }
    }

    public Bundle build() {
        int responseCode = responseCode();
        if (responseCode == APIOverrides.RESULT_DEFAULT) {
            responseCode = GoogleUtil.RESULT_OK;
        }
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode);
        if (responseCode == GoogleUtil.RESULT_OK) {
            bundle.putStringArrayList(GoogleUtil.DETAILS_LIST, new ArrayList<>(detailsList));
        }
        return bundle;
    }

    @Override
    protected int rawResponseCode() {
        return apiOverrides.getGetSkuDetailsResponse();
    }
}
