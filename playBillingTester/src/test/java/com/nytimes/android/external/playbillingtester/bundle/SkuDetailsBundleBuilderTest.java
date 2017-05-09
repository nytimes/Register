package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ConfigSku;
import com.nytimes.android.external.playbillingtester.model.ImmutableConfigSku;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static com.nytimes.android.external.playbillingtester.di.GsonFactory.create;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SkuDetailsBundleBuilderTest extends TestCase {

    private static final String SKU1 = "sku1";
    private static final String SKU2 = "sku2";
    private static final String TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;

    private SkuDetailsBundleBuilder testObject;

    @Mock
    private APIOverridesAndPurchases apiOverridesAndPurchases;

    @Mock
    private Config config;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        String packageName = "com.my.pkg";
        String description = "some description";
        String title = "caps for sale";
        String price = "1.98";

        testObject = new SkuDetailsBundleBuilder(apiOverridesAndPurchases, config, create());
        when(config.skus()).thenReturn(new ImmutableMap.Builder<String, ConfigSku>()
                .put(SKU1, ImmutableConfigSku.builder()
                        .itemType(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .put(SKU2, ImmutableConfigSku.builder()
                        .itemType(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .build());
    }

    @Test
    public void testBundleOK() {
        when(apiOverridesAndPurchases.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_OK);

        Bundle bundle = testObject.newBuilder()
                .skus(ImmutableList.of(SKU1, SKU2), TYPE)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        ArrayList<String> detailsList =  bundle.getStringArrayList(GoogleUtil.DETAILS_LIST);
        assertThat(detailsList.get(0))
                .isEqualTo("{\"productId\":\"sku1\",\"itemType\":\"subs\",\"price\":\"$1.98\"," +
                        "\"title\":\"caps for sale\",\"description\":\"some description\"," +
                        "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}");
        assertThat(detailsList.get(1))
                .isEqualTo("{\"productId\":\"sku2\",\"itemType\":\"subs\",\"price\":\"$1.98\"," +
                        "\"title\":\"caps for sale\",\"description\":\"some description\"," +
                        "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}");
    }

    @Test
    public void testBundleNotOK() {
        when(apiOverridesAndPurchases.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

        Bundle bundle = testObject.newBuilder()
                .skus(ImmutableList.of(SKU1, SKU2), TYPE)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR);
        assertThat(bundle.getStringArrayList(GoogleUtil.DETAILS_LIST))
                .isNull();
    }

    @Test
    public void testRawResponseCode() {
        testObject.rawResponseCode();
        verify(apiOverridesAndPurchases).getGetSkuDetailsResponse();
    }
}

