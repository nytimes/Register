package com.nytimes.android.external.playbillingtesterlib;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.nytimes.android.external.playbillingtester.IInAppBillingService;

import java.util.List;

/**
 * methods calls against the fake (test) google in app billing service.
 */
public class GoogleServiceProviderTesting implements GoogleServiceProvider  {

    private static final String INTENT_STRING =
        "com.nytimes.android.external.playbillingtester.InAppBillingService.BIND";
    private static final String INTENT_PKG = "com.nytimes.android.external.playbillingtester";
    private IInAppBillingService billingService;
    public GoogleServiceProviderTesting() {
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent(INTENT_STRING);
        intent.setPackage(INTENT_PKG);
        return intent;
    }

    @Override
    public void initService(IBinder service) {
        billingService = IInAppBillingService.Stub.asInterface(service);
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName, String type)
        throws RemoteException {
        return billingService.isBillingSupported(apiVersion, packageName, type);
    }

    @Override
    public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
                               String developerPayload) throws RemoteException{
        return billingService.getBuyIntent(apiVersion, packageName, sku, type, developerPayload);
    }

    @Override
    public void releaseService() {
        billingService = null;
    }

    @Override
    public Bundle getPurchases(int apiVersion, String packageName, String type,
                               String continuationToken) throws RemoteException {
        return billingService.getPurchases(apiVersion, packageName, type, continuationToken);
    }
    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName, String type, Bundle skusBundle)
        throws RemoteException {
        return billingService.getSkuDetails(apiVersion, packageName, type, skusBundle);
    }

    @Override
    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) throws RemoteException {
        return billingService.consumePurchase(apiVersion, packageName, purchaseToken);
    }

    @Override
    public Bundle getBuyIntentToReplaceSkus(int apiVersion, String packageName, List<String> oldSkus, String newSku,
                                            String type, String developerPayload) throws RemoteException {
        return billingService
                .getBuyIntentToReplaceSkus(apiVersion, packageName, oldSkus, newSku, type, developerPayload);
    }
}
