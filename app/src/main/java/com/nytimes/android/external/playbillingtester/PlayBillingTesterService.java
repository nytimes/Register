package com.nytimes.android.external.playbillingtester;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.nytimes.android.external.playbillingtester.di.Injector;

import javax.inject.Inject;


/**
 * Service that handles Play Billing Tester API calls
 */
public class PlayBillingTesterService extends Service {
    private static final String TOAST_FMT = "%s %s";

    @Inject
    protected IInAppBillingService.Stub mBinder;

    public PlayBillingTesterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, String.format(TOAST_FMT, getString(R.string.app_name), getString(R.string.destroyed)),
                Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, String.format(TOAST_FMT, getString(R.string.app_name), getString(R.string.created)),
                Toast.LENGTH_SHORT).show();
        inject();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, String.format(TOAST_FMT, getString(R.string.app_name), getString(R.string.started)),
                Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }
}
