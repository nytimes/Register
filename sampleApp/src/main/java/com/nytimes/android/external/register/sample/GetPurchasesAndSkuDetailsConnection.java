package com.nytimes.android.external.register.sample;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nytimes.android.external.registerlib.GoogleServiceProvider;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class GetPurchasesAndSkuDetailsConnection implements ServiceConnection {

    private static final String TAG = "GetPurchasesConnection";
    private final GoogleServiceProvider googleServiceProvider;
    private final PublishSubject<Response> purchasesAndSkuDetailsSubject;
    private final List<String> iapSkus, subSkus;
    private final String packageName;

    public GetPurchasesAndSkuDetailsConnection(List<String> iapSkus, List<String> subSkus, String packageName,
                                               GoogleServiceProvider googleServiceProvider) {
        this.iapSkus = iapSkus;
        this.subSkus = subSkus;
        this.packageName = packageName;
        this.googleServiceProvider = googleServiceProvider;
        this.purchasesAndSkuDetailsSubject = PublishSubject.create();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        googleServiceProvider.initService(service);
        try {
            purchasesAndSkuDetailsSubject.onNext(ImmutableResponse.builder()
                    .subSkuDetails(googleServiceProvider.getSkuDetails(GoogleUtil.BILLING_API_VERSION,
                            packageName, GoogleUtil.BILLING_TYPE_SUBSCRIPTION, makeItemBundle(subSkus)))
                    .iapSkuDetails(googleServiceProvider.getSkuDetails(GoogleUtil.BILLING_API_VERSION,
                            packageName, GoogleUtil.BILLING_TYPE_IAP, makeItemBundle(iapSkus)))
                    .subPurchases(googleServiceProvider.getPurchases(GoogleUtil.BILLING_API_VERSION,
                            packageName, GoogleUtil.BILLING_TYPE_SUBSCRIPTION, ""))
                    .iapPurchases(googleServiceProvider.getPurchases(GoogleUtil.BILLING_API_VERSION,
                            packageName, GoogleUtil.BILLING_TYPE_IAP, ""))
                    .build());
        } catch (RemoteException e) {
            Log.e(TAG, "Error on getPurchases", e);
        }
    }

    private Bundle makeItemBundle(List<String> skus) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(GoogleUtil.ITEM_ID_LIST, new ArrayList<>(skus));
        return bundle;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        googleServiceProvider.releaseService();
    }

    Observable<Response> getPurchasesAndSkuDetails() {
        return purchasesAndSkuDetailsSubject;
    }

    @Value.Immutable
    static class Response {
        @Value.Default
        Bundle iapSkuDetails() {
            return new Bundle();
        }

        @Value.Default
        Bundle subSkuDetails()  {
            return new Bundle();
        }

        @Value.Default
        Bundle iapPurchases() {
            return new Bundle();
        }

        @Value.Default
        Bundle subPurchases() {
            return new Bundle();
        }
    }
}
