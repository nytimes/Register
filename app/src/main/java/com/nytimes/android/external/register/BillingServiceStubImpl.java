package com.nytimes.android.external.register;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder;
import com.nytimes.android.external.register.bundle.ConsumePurchaseResponse;
import com.nytimes.android.external.register.bundle.PurchasesBundleBuilder;
import com.nytimes.android.external.register.bundle.SkuDetailsBundleBuilder;
import com.nytimes.android.external.registerlib.GoogleUtil;

import java.util.List;

import javax.inject.Inject;

public class BillingServiceStubImpl extends IInAppBillingService.Stub {

    private final APIOverrides apiOverrides;
    private final BuyIntentBundleBuilder buyIntentBundleBuilder;
    private final SkuDetailsBundleBuilder skuDetailsBundleBuilder;
    private final PurchasesBundleBuilder purchasesBundleBuilder;
    private final ConsumePurchaseResponse consumePurchaseResponse;
    private final BuyIntentToReplaceSkusBundleBuilder buyIntentToReplaceSkusBundleBuilder;

    @Inject
    public BillingServiceStubImpl(APIOverrides apiOverrides,
                                  BuyIntentBundleBuilder buyIntentBundleBuilder,
                                  SkuDetailsBundleBuilder skuDetailsBundleBuilder,
                                  PurchasesBundleBuilder purchasesBundleBuilder,
                                  ConsumePurchaseResponse consumePurchaseResponse,
                                  BuyIntentToReplaceSkusBundleBuilder buyIntentToReplaceSkusBundleBuilder) {
        this.apiOverrides = apiOverrides;
        this.buyIntentBundleBuilder = buyIntentBundleBuilder;
        this.skuDetailsBundleBuilder = skuDetailsBundleBuilder;
        this.purchasesBundleBuilder = purchasesBundleBuilder;
        this.consumePurchaseResponse = consumePurchaseResponse;
        this.buyIntentToReplaceSkusBundleBuilder = buyIntentToReplaceSkusBundleBuilder;
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName, String type) {
        int response = apiOverrides.isBillingSupportedResponse();
        if (response == APIOverrides.RESULT_DEFAULT) {
            response = apiVersion <= GoogleUtil.BILLING_API_VERSION ?
                    GoogleUtil.RESULT_OK : GoogleUtil.RESULT_BILLING_UNAVAILABLE;
        }
        return response;
    }

    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName, String type, Bundle skusBundle) {
        return skuDetailsBundleBuilder
                .newBuilder()
                .skus(skusBundle.getStringArrayList(GoogleUtil.ITEM_ID_LIST), type)
                .build();
    }

    @Override
    public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
                               String developerPayload) {
        return buyIntentBundleBuilder
                .newBuilder()
                .packageName(packageName)
                .sku(sku)
                .type(type)
                .developerPayload(developerPayload)
                .build();
    }

    @Override
    public Bundle getPurchases(int apiVersion, String packageName, String type, String continuationToken) {
        return purchasesBundleBuilder
                .newBuilder()
                .type(type)
                .continuationToken(continuationToken)
                .build();
    }

    @Override
    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) {
        return consumePurchaseResponse.consumePurchase(apiVersion, packageName, purchaseToken);
    }

    @Override
    public int stub(int apiVersion, String packageName, String type) throws RemoteException {
        // We are not using this call
        return 0;
    }

    @Override
    public Bundle getBuyIntentToReplaceSkus(int apiVersion, String packageName, List<String> oldSkus,
                                            String newSku, String type,
                                            String developerPayload) throws RemoteException {
        return buyIntentToReplaceSkusBundleBuilder
                .newBuilder()
                .packageName(packageName)
                .oldSkus(oldSkus)
                .newSku(newSku)
                .type(type)
                .developerPayload(developerPayload)
                .build();
    }

    @Override
    public IBinder asBinder() {
        return this;
    }
}
