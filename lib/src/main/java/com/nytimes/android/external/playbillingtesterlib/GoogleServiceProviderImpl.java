package com.nytimes.android.external.playbillingtesterlib;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.vending.billing.IInAppBillingService;

/**
 * methods calls against the real google in app billing service
 */
public class GoogleServiceProviderImpl implements GoogleServiceProvider  {

    private static final String INTENT_STRING =
        "com.android.vending.billing.InAppBillingService.BIND";
    private static final String INTENT_PKG = "com.android.vending";
    private IInAppBillingService billingService;

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
    public Bundle getPurchases(int apiVersion, String packageName, String type,
                               String continuationToken) throws RemoteException {
        return billingService.getPurchases(apiVersion, packageName, type, continuationToken);
    }

    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName, String type,
                                Bundle skusBundle) throws RemoteException {
        return billingService.getSkuDetails(apiVersion, packageName, type, skusBundle);
    }

    @Override
    public void releaseService() {
        billingService = null;
    }
}
