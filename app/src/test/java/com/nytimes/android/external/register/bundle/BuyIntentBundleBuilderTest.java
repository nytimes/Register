package com.nytimes.android.external.register.bundle;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPendingIntent;

import static com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD;
import static com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.EX_ITEM_TYPE;
import static com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.EX_PACKAGE_NAME;
import static com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder.EX_SKU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BuyIntentBundleBuilderTest {

    private static final String PACKAGE_NAME = "com.my.pkg";
    private static final String SKU = "sku1";
    private static final String TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;
    private static final String DEVELOPER_PAYLOAD = "devPayload";

    private BuyIntentBundleBuilder testObject;

    @Mock
    private APIOverrides apiOverrides;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObject = new BuyIntentBundleBuilder(RuntimeEnvironment.application, apiOverrides);
    }

    @Test
    public void testBundleOK() {
        when(apiOverrides.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_OK);

        Bundle bundle = testObject.newBuilder()
                .packageName(PACKAGE_NAME)
                .sku(SKU)
                .type(TYPE)
                .developerPayload(DEVELOPER_PAYLOAD)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        PendingIntent pendingIntent = bundle.getParcelable(GoogleUtil.BUY_INTENT);
        Intent intent = getIntent(pendingIntent);
        assertThat(intent.getStringExtra(EX_PACKAGE_NAME))
                .isEqualTo(PACKAGE_NAME);
        assertThat(intent.getStringExtra(EX_SKU))
                .isEqualTo(SKU);
        assertThat(intent.getStringExtra(EX_ITEM_TYPE))
                .isEqualTo(TYPE);
        assertThat(intent.getStringExtra(EX_DEVELOPER_PAYLOAD))
                .isEqualTo(DEVELOPER_PAYLOAD);
    }

    @Test
    public void testBundleNotOK() {
        when(apiOverrides.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

        Bundle bundle = testObject.newBuilder()
                .packageName(PACKAGE_NAME)
                .sku(SKU)
                .type(TYPE)
                .developerPayload(DEVELOPER_PAYLOAD)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR);
        assertThat((Intent) bundle.getParcelable(GoogleUtil.BUY_INTENT))
                .isNull();
    }

    @Test
    public void testRawResponseCode() {
        testObject.rawResponseCode();
        verify(apiOverrides).getGetBuyIntentResponse();
    }

    public static Intent getIntent(PendingIntent pendingIntent) {
        return ((ShadowPendingIntent) Shadow.extract(pendingIntent)).getSavedIntent();
    }
}

