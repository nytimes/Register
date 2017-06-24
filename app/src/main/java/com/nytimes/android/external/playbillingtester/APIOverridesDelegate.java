package com.nytimes.android.external.playbillingtester;


import android.support.annotation.IdRes;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class APIOverridesDelegate {

    private final APIOverrides apiOverrides;

    @Inject
    APIOverridesDelegate(APIOverrides apiOverrides) {
        this.apiOverrides = apiOverrides;
    }

    void setApiOverridesValue(@IdRes int containerLayoutId, ConfigResponse item) {
        switch (containerLayoutId) {
            case R.id.isBillingSupported:
                apiOverrides.setIsBillingSupportedResponse(item.responseCode());
                break;
            case R.id.getBuyIntent:
                apiOverrides.setGetBuyIntentResponse(item.responseCode());
                break;
            case R.id.buy:
                apiOverrides.setBuyResponse(item.responseCode());
                break;
            case R.id.getPurchases:
                apiOverrides.setGetPurchasesResponse(item.responseCode());
                break;
            case R.id.getSkuDetails:
                apiOverrides.setGetSkuDetailsResponse(item.responseCode());
                break;
            case R.id.consumePurchase:
                apiOverrides.setConsumePurchaseResponse(item.responseCode());
                break;
            case R.id.getBuyIntentToReplaceSkus:
                apiOverrides.setGetBuyIntentToReplaceSkusResponse(item.responseCode());
                break;
            default:
                // unknown id
                break;
        }
    }

    int getApiOverridesValue(@IdRes int containerLayoutId) {
        int apiValue;
        switch (containerLayoutId) {
            case R.id.isBillingSupported:
                apiValue = apiOverrides.getIsBillingSupportedResponse();
                break;
            case R.id.getBuyIntent:
                apiValue = apiOverrides.getGetBuyIntentResponse();
                break;
            case R.id.buy:
                apiValue = apiOverrides.getBuyResponse();
                break;
            case R.id.getPurchases:
                apiValue = apiOverrides.getGetPurchasesResponse();
                break;
            case R.id.getSkuDetails:
                apiValue = apiOverrides.getGetSkuDetailsResponse();
                break;
            case R.id.consumePurchase:
                apiValue = apiOverrides.getConsumePurchaseResponse();
                break;
            case R.id.getBuyIntentToReplaceSkus:
                apiValue = apiOverrides.getGetBuyIntentToReplaceSkusResponse();
                break;
            default:
                apiValue = -1;
                break;
        }
        return apiValue;
    }
}
