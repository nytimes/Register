package com.nytimes.android.external.playbillingtester.sample;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProvider;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.reactivex.observers.TestObserver;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BuyServiceConnectionTest {

    private BuyServiceConnection testObject;
    @Mock
    private ComponentName componentName;
    @Mock
    private IBinder binder;
    @Mock
    private GoogleServiceProvider googleServiceProvider;

    private PendingIntent pendingIntent;
    private TestObserver<PendingIntent> testObserver;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testObject = new BuyServiceConnection("sku", "com.my.pkg", "devPayload", GoogleUtil.BILLING_TYPE_SUBSCRIPTION,
                googleServiceProvider);
        Intent intent = new Intent(RuntimeEnvironment.application, SampleActivity.class);
        Bundle buyBundle = new Bundle();
        pendingIntent = PendingIntent.getActivity(RuntimeEnvironment.application, 0, intent, 0);
        buyBundle.putParcelable(GoogleUtil.BUY_INTENT, pendingIntent);
        when(googleServiceProvider.getBuyIntent(anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(buyBundle);
        testObserver = TestObserver.create();
    }

    @Test
    public void testActivityStartsWhenBillingSupported() throws Exception {
        when(googleServiceProvider.isBillingSupported(anyInt(), anyString(), anyString()))
                .thenReturn(GoogleUtil.RESULT_OK);
        testObject.getBuyPendingIntent().subscribe(testObserver);
        testObject.onServiceConnected(componentName, binder);
        testObserver.assertValue(pendingIntent);
    }

    @Test
    public void testActivityDoesntStartWhenBillingNotSupported() throws Exception {
        when(googleServiceProvider.isBillingSupported(anyInt(), anyString(), anyString()))
                .thenReturn(GoogleUtil.RESULT_ERROR);
        testObject.getBuyPendingIntent().subscribe(testObserver);
        testObject.onServiceConnected(componentName, binder);
        testObserver.assertNoValues();
    }
}
