package com.nytimes.android.external.playbillingtester.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProvider;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class GetPurchasesAndSkuDetailsConnection implements ServiceConnection {

    private static final String TAG = "GetPurchasesConnection";
    private final GoogleServiceProvider googleServiceProvider;
    private final Activity activity;
    private final PublishSubject<Response> purchasesAndSkuDetailsSubject;
    private final List<String> iapSkus, subSkus;

    public GetPurchasesAndSkuDetailsConnection(List<String> iapSkus, List<String> subSkus, Activity activity,
                                               GoogleServiceProvider googleServiceProvider) {
        this.iapSkus = iapSkus;
        this.subSkus = subSkus;
        this.googleServiceProvider = googleServiceProvider;
        this.activity = activity;
        this.purchasesAndSkuDetailsSubject = PublishSubject.create();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        googleServiceProvider.initService(service);
        try {
            purchasesAndSkuDetailsSubject.onNext(ImmutableResponse.builder()
                    .subSkuDetails(googleServiceProvider.getSkuDetails(GoogleUtil.BILLING_API_VERSION,
                            activity.getPackageName(), GoogleUtil.BILLING_TYPE_SUBSCRIPTION, makeItemBundle(subSkus)))
                    .iapSkuDetails(googleServiceProvider.getSkuDetails(GoogleUtil.BILLING_API_VERSION,
                            activity.getPackageName(), GoogleUtil.BILLING_TYPE_IAP, makeItemBundle(iapSkus)))
                    .subPurchases(googleServiceProvider.getPurchases(GoogleUtil.BILLING_API_VERSION,
                            activity.getPackageName(), GoogleUtil.BILLING_TYPE_SUBSCRIPTION,
                            GoogleUtil.CONTINUATION_TOKEN))
                    .iapPurchases(googleServiceProvider.getPurchases(GoogleUtil.BILLING_API_VERSION,
                            activity.getPackageName(), GoogleUtil.BILLING_TYPE_IAP,
                            GoogleUtil.CONTINUATION_TOKEN))
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

    public Observable<Response> getPurchasesAndSkuDetails() {
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
