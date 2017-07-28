package com.nytimes.android.external.register.sample;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nytimes.android.external.registerlib.GoogleServiceProvider;
import com.nytimes.android.external.registerlib.GoogleUtil;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class BuyServiceConnection implements ServiceConnection {

    public static final int REQUEST_CODE_GOOGLE_PURCHASE = 21001;
    private static final String TAG = "BuyServiceConnection";

    private final String sku;
    private final String type;
    private final String packageName;
    private final String developerPayload;
    private final GoogleServiceProvider googleServiceProvider;
    private final PublishSubject<PendingIntent> buyPendingIntent;

    public BuyServiceConnection(String sku, String type, String packageName, String developerPayload,
                                GoogleServiceProvider googleServiceProvider) {
        this.sku = sku;
        this.type = type;
        this.packageName = packageName;
        this.developerPayload = developerPayload;
        this.googleServiceProvider = googleServiceProvider;
        this.buyPendingIntent = PublishSubject.create();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        googleServiceProvider.initService(service);
        try {
            int apiVer = GoogleUtil.BILLING_API_VERSION;
            int result = googleServiceProvider.isBillingSupported(apiVer, packageName, type);
            if (result == GoogleUtil.RESULT_OK) {
                Bundle buyIntentBundle =
                        googleServiceProvider.getBuyIntent(apiVer, packageName, sku, type, developerPayload);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable(GoogleUtil.BUY_INTENT);
                buyPendingIntent.onNext(pendingIntent);
            } else {
                Log.e(TAG, "Buy returned " + result);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error on buy", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        googleServiceProvider.releaseService();
    }

    public Observable<PendingIntent> getBuyPendingIntent() {
        return buyPendingIntent;
    }
}
