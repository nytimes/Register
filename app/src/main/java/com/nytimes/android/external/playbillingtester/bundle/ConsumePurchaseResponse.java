package com.nytimes.android.external.playbillingtester.bundle;

import com.nytimes.android.external.playbillingtester.APIOverrides;
import com.nytimes.android.external.playbillingtester.Purchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import java.util.List;

import javax.inject.Inject;

import static com.nytimes.android.external.playbillingtesterlib.GoogleUtil.RESULT_OK;

public class ConsumePurchaseResponse extends BaseResponse {

    private final Purchases purchases;

    @Inject
    public ConsumePurchaseResponse(APIOverrides apiOverrides, Purchases purchases) {
        super(apiOverrides);
        this.purchases = purchases;
    }

    @Override
    protected int rawResponseCode() {
        return apiOverrides.getConsumePurchaseResponse();
    }

    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) {
        int responseCode = responseCode();
        List<String> inAppPurchaseItems, subscriptionsItems;
        if (responseCode == RESULT_OK) {
            subscriptionsItems = purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null).purchaseDataList();
            if (subscriptionsItems.contains(purchaseToken)) {
                return GoogleUtil.RESULT_ERROR;
            }
            inAppPurchaseItems = purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null).purchaseDataList();
            if (inAppPurchaseItems.contains(purchaseToken)) {
                if (!purchases.removePurchase(purchaseToken, GoogleUtil.BILLING_TYPE_IAP)) {
                    return GoogleUtil.RESULT_ERROR;
                }
            } else {
                return GoogleUtil.RESULT_ITEM_NOT_OWNED;
            }
        }
        return responseCode();
    }

}
