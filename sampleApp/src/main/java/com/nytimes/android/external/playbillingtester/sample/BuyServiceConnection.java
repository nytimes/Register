package com.nytimes.android.external.playbillingtester.sample;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProvider;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

public class BuyServiceConnection implements ServiceConnection {

    public static final int REQUEST_CODE_GOOGLE_PURCHASE = 21001;
    private static final String TAG = "BuyServiceConnection";

    private final String sku;
    private final String type;
    private final GoogleServiceProvider googleServiceProvider;
    private final Activity activity;

    public BuyServiceConnection(String sku, String type, Activity activity,
                                GoogleServiceProvider googleServiceProvider) {
        this.sku = sku;
        this.type = type;
        this.googleServiceProvider = googleServiceProvider;
        this.activity = activity;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        googleServiceProvider.initService(service);
        try {
            int result = googleServiceProvider
                    .isBillingSupported(GoogleUtil.BILLING_API_VERSION, activity.getPackageName(), type);
            if (result == GoogleUtil.RESULT_OK) {
                Bundle buyIntentBundle = googleServiceProvider.getBuyIntent(
                        GoogleUtil.BILLING_API_VERSION, activity.getPackageName(),
                        sku, type, GoogleUtil.BILLING_DEVELOPER_PAYLOAD);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable(GoogleUtil.BUY_INTENT);
                activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                        REQUEST_CODE_GOOGLE_PURCHASE, new Intent(), 0, 0, 0);
            } else {
                Log.e(TAG, "Buy returned " + result);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error on buy", e);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Error on buy", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        googleServiceProvider.releaseService();
    }
}
