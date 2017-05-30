package com.nytimes.android.external.playbillingtesterlib;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.io.Serializable;

/**
 * Interface above real test Google Service (in app billing)
 */
public interface GoogleServiceProvider extends Serializable {
    Intent getIntent();
    void initService(IBinder service);
    int isBillingSupported(int apiVersion, String packageName, String type) throws RemoteException;
    Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
                        String developerPayload) throws RemoteException;
    Bundle getPurchases(int apiVersion, String packageName, String type, String
            continuationToken) throws RemoteException;
    public Bundle getSkuDetails(int apiVersion, String packageName, String type,
                                Bundle skusBundle) throws RemoteException;
    int consumePurchase(int apiVersion, String packageName,
                        String purchaseToken) throws RemoteException;
    void releaseService();
}
