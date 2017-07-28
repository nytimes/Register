package com.nytimes.android.external.register.bundle;

import android.os.Bundle;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.Purchases;
import com.nytimes.android.external.registerlib.GoogleUtil;

import javax.inject.Inject;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class PurchasesBundleBuilder extends BaseBundleBuilder {

    private String type;
    private String continuationToken;
    private final Purchases purchases;

    @Inject
    public PurchasesBundleBuilder(APIOverrides apiOverrides, Purchases purchases) {
        super(apiOverrides);
        this.purchases = purchases;
    }

    public PurchasesBundleBuilder newBuilder() {
        bundle = new Bundle();
        return this;
    }

    public PurchasesBundleBuilder type(String type) {
        this.type = type;
        return this;
    }

    public PurchasesBundleBuilder continuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    public Bundle build() {
        int responseCode = responseCode();
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode);

        if (responseCode == GoogleUtil.RESULT_OK) {
            Purchases.PurchasesLists purchasesLists = purchases.getPurchasesLists(type, continuationToken);
            bundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_ITEM_LIST,
                    Lists.newArrayList(purchasesLists.purchaseItemList()));
            bundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST,
                    Lists.newArrayList(purchasesLists.purchaseDataList()));
            bundle.putStringArrayList(GoogleUtil.INAPP_DATA_SIGNATURE_LIST,
                    Lists.newArrayList(purchasesLists.dataSignatureList()));
            if (!Strings.isNullOrEmpty(purchasesLists.continuationToken())) {
                bundle.putString(GoogleUtil.INAPP_CONTINUATION_TOKEN, purchasesLists.continuationToken());
            }
        }
        return bundle;
    }

    @Override
    protected int rawResponseCode() {
        return apiOverrides.getGetPurchasesResponse();
    }
}
