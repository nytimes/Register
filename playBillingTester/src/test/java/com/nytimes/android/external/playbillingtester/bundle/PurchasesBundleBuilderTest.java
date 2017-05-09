package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchasesBundleBuilderTest extends TestCase {

    private PurchasesBundleBuilder testObject;

    @Mock
    private APIOverridesAndPurchases apiOverridesAndPurchases;

    private final String type = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;
    private final List<String> purchases = ImmutableList.of("purchase1", "purchase2");

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        testObject = new PurchasesBundleBuilder(apiOverridesAndPurchases);
    }

    @Test
    public void testBundleOK() {
        when(apiOverridesAndPurchases.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(apiOverridesAndPurchases.getInAppPurchaseDataAsArrayList(type)).thenReturn(purchases);
        Bundle bundle = testObject.newBuilder()
                .type(type)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(purchases);
    }

    @Test
    public void testBundleNotOK() {
        when(apiOverridesAndPurchases.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

        Bundle bundle = testObject.newBuilder()
                .type(type)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR);
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isNull();
    }

    @Test
    public void testRawResponseCode() {
        testObject.rawResponseCode();
        verify(apiOverridesAndPurchases).getGetPurchasesResponse();
    }
}

