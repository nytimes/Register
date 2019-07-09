package com.nytimes.android.external.register.bundle;

import android.os.Bundle;

import com.google.common.base.Optional;
import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.model.Config;
import com.nytimes.android.external.register.model.ConfigSku;
import com.nytimes.android.external.registerlib.GoogleProductResponse;
import com.nytimes.android.external.registerlib.GoogleUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SkuDetailsBundleBuilder extends BaseBundleBuilder {

    private List<String> detailsList;
    private final Optional<Config> config;

    @Inject
    public SkuDetailsBundleBuilder(APIOverrides apiOverrides, Optional<Config> config) {
        super(apiOverrides);
        this.config = config;
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
            GoogleProductResponse.Builder builder = new GoogleProductResponse.Builder()
                    .productId(sku)
                    .itemType(type)
                    .description(configSku.description())
                    .title(configSku.title())
                    .price("$" + configSku.price())
                    .priceAmountMicros((int) (Double.parseDouble(configSku.price()) * 1000000))
                    .subscriptionPeriod(configSku.subscriptionPeriod())
                    .freeTrialPeriod(configSku.freeTrialPeriod())
                    .priceCurrencyCode("USD");

            if (configSku.introductoryPrice() != null) {
                builder.introductoryPrice("$" + configSku.introductoryPrice())
                        .introductoryPriceAmountMicros((int) (Double.parseDouble(configSku
                                .introductoryPrice()) * 1000000))
                        .introductoryPriceCycles(configSku.introductoryPriceCycles())
                        .introductoryPricePeriod(configSku.introductoryPricePeriod());
            }

            GoogleProductResponse googleProductResponse = builder.build();
            detailsList.add(GoogleProductResponse.toJson(googleProductResponse));
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
