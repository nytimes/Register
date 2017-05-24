package com.nytimes.android.external.playbillingtester;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.ConsumePurchaseResponse;
import com.nytimes.android.external.playbillingtester.bundle.PurchasesBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.SkuDetailsBundleBuilder;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import java.util.List;

import javax.inject.Inject;

public class BillingServiceStubImpl extends IInAppBillingService.Stub {

    final Gson gson;
    final Config config;
    private final APIOverridesAndPurchases apiOverridesAndPurchases;
    private final BuyIntentBundleBuilder buyIntentBundleBuilder;
    private final SkuDetailsBundleBuilder skuDetailsBundleBuilder;
    private final PurchasesBundleBuilder purchasesBundleBuilder;
    private final ConsumePurchaseResponse consumePurchaseResponse;

    @Inject
    public BillingServiceStubImpl(APIOverridesAndPurchases apiOverridesAndPurchases, Gson gson, Config config,
                                  BuyIntentBundleBuilder buyIntentBundleBuilder,
                                  SkuDetailsBundleBuilder skuDetailsBundleBuilder,
                                  PurchasesBundleBuilder purchasesBundleBuilder,
                                  ConsumePurchaseResponse consumePurchaseResponse) {
        this.apiOverridesAndPurchases = apiOverridesAndPurchases;
        this.gson = gson;
        this.config = config;
        this.buyIntentBundleBuilder = buyIntentBundleBuilder;
        this.skuDetailsBundleBuilder = skuDetailsBundleBuilder;
        this.purchasesBundleBuilder = purchasesBundleBuilder;
        this.consumePurchaseResponse = consumePurchaseResponse;
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName, String type) {
        int response = apiOverridesAndPurchases.getIsBillingSupportedResponse();
        if (response == APIOverridesAndPurchases.RESULT_DEFAULT) {
            response = GoogleUtil.RESULT_OK;
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
        // We are not using this call
        return null;
    }

    @Override
    public IBinder asBinder() {
        return this;
    }
}
