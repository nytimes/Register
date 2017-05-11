package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.google.common.collect.Lists;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class PurchasesBundleBuilder extends BaseBundleBuilder {

    private String type;

    @Inject
    public PurchasesBundleBuilder(APIOverridesAndPurchases apiOverridesAndPurchases) {
        super(apiOverridesAndPurchases);
    }

    public PurchasesBundleBuilder newBuilder() {
        bundle = new Bundle();
        return this;
    }

    public PurchasesBundleBuilder type(String type) {
        this.type = type;
        return this;
    }

    public Bundle build() {
        int responseCode = responseCode();
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode);

        if (responseCode == GoogleUtil.RESULT_OK) {
            bundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST,
                    Lists.newArrayList(apiOverridesAndPurchases.getInAppPurchaseDataAsArrayList(type)));
        }
        return bundle;
    }

    @Override
    protected int rawResponseCode() {
        return apiOverridesAndPurchases.getGetPurchasesResponse();
    }
}
