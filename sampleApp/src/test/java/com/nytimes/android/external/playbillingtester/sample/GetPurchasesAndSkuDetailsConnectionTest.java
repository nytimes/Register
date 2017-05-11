package com.nytimes.android.external.playbillingtester.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProvider;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GetPurchasesAndSkuDetailsConnectionTest {

    private GetPurchasesAndSkuDetailsConnection testObject;
    @Mock
    private Activity activity;
    @Mock
    private GoogleServiceProvider googleServiceProvider;
    @Mock
    private ComponentName componentName;
    @Mock
    private IBinder binder;

    private final StringMatcher iapMatcher = new StringMatcher(GoogleUtil.BILLING_TYPE_IAP);
    private final StringMatcher subMatcher = new StringMatcher(GoogleUtil.BILLING_TYPE_SUBSCRIPTION);

    private final Bundle iapSkuDetailsBundle = new Bundle(), subSkuDetailsBundle = new Bundle(),
            iapPurchasesBundle = new Bundle(), subPurchasesBundle = new Bundle();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testObject = new GetPurchasesAndSkuDetailsConnection(ImmutableList.of("sku-iap"),
                ImmutableList.of("sku-sub"), activity, googleServiceProvider);
        when(googleServiceProvider.getSkuDetails(anyInt(), anyString(), argThat(iapMatcher), any(Bundle.class)))
                .thenReturn(iapSkuDetailsBundle);
        when(googleServiceProvider.getSkuDetails(anyInt(), anyString(), argThat(subMatcher), any(Bundle.class)))
                .thenReturn(subSkuDetailsBundle);
        when(googleServiceProvider.getPurchases(anyInt(), anyString(), argThat(iapMatcher), anyString()))
                .thenReturn(iapPurchasesBundle);
        when(googleServiceProvider.getPurchases(anyInt(), anyString(), argThat(subMatcher), anyString()))
                .thenReturn(subPurchasesBundle);
    }

    @Test
    public void testOnServiceConnectedReturnsExpectedBundles() throws Exception {
        TestObserver<GetPurchasesAndSkuDetailsConnection.Response> observer = TestObserver.create();
        testObject
                .getPurchasesAndSkuDetails()
                .subscribe(observer);
        testObject.onServiceConnected(componentName, binder);
        verify(googleServiceProvider).initService(binder);
        observer.assertValueCount(1);
        GetPurchasesAndSkuDetailsConnection.Response response = observer.values().get(0);
        assertThat(iapSkuDetailsBundle).isEqualTo(response.iapSkuDetails());
        assertThat(subSkuDetailsBundle).isEqualTo(response.subSkuDetails());
        assertThat(iapPurchasesBundle).isEqualTo(response.iapPurchases());
        assertThat(subPurchasesBundle).isEqualTo(response.subPurchases());
    }

    static class IapTypMatcher extends BaseMatcher<String> {
        @Override
        public boolean matches(Object item) {
            return GoogleUtil.BILLING_TYPE_IAP.equals(item);
        }

        @Override
        public void describeTo(Description description) {
            // intentionally blank
        }
    }

    static class StringMatcher extends BaseMatcher<String> {
        private final String str;

        StringMatcher(String str) {
            this.str = str;
        }

        @Override
        public boolean matches(Object item) {
            return str.equals(item);
        }

        @Override
        public void describeTo(Description description) {
            // intentionally blank
        }
    }
}
