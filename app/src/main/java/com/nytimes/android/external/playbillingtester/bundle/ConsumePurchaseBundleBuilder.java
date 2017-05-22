package com.nytimes.android.external.playbillingtester.bundle;

import com.google.common.collect.Lists;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import java.util.List;

import javax.inject.Inject;

import static com.nytimes.android.external.playbillingtesterlib.GoogleUtil.RESULT_OK;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class ConsumePurchaseBundleBuilder extends BaseBundleBuilder {

    @Inject
    public ConsumePurchaseBundleBuilder(APIOverridesAndPurchases apiOverridesAndPurchases) {
        super(apiOverridesAndPurchases);
    }

    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) {
        int responseCode = responseCode();
        List<String> inAppPurchaseItems, subscriptionsItems;
        if (responseCode == RESULT_OK) {
            subscriptionsItems = Lists.newArrayList(apiOverridesAndPurchases.getInAppPurchaseDataAsArrayList(GoogleUtil.BILLING_TYPE_SUBSCRIPTION));
            if (subscriptionsItems.contains(purchaseToken)) {
                return GoogleUtil.RESULT_ERROR;
            }
            inAppPurchaseItems = Lists.newArrayList(apiOverridesAndPurchases.getInAppPurchaseDataAsArrayList(GoogleUtil.BILLING_TYPE_IAP));
            if (inAppPurchaseItems.contains(purchaseToken)) {
                apiOverridesAndPurchases.removePurchase(purchaseToken, GoogleUtil.BILLING_TYPE_IAP);
            } else {
                return GoogleUtil.RESULT_ITEM_NOT_OWNED;
            }
        }
        return responseCode();
    }

    @Override
    protected int rawResponseCode() {
        return apiOverridesAndPurchases.getConsumePurchaseResponse();
    }
}
