package com.nytimes.android.external.register.bundle;

import android.os.Bundle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.model.Config;
import com.nytimes.android.external.register.model.ConfigSku;
import com.nytimes.android.external.register.model.ImmutableConfigSku;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.ArrayList;

import io.reactivex.exceptions.Exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SkuDetailsBundleBuilderTest {

    private static final String SKU1 = "sku1";
    private static final String SKU2 = "sku2";
    private static final String TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;

    private SkuDetailsBundleBuilder testObject;

    @Mock
    private APIOverrides apiOverrides;

    @Mock
    private Config config;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        String packageName = "com.my.pkg";
        String description = "some description";
        String title = "caps for sale";
        String price = "1.98";

        testObject = new SkuDetailsBundleBuilder(apiOverrides, Optional.of(config));
        when(config.skus()).thenReturn(new ImmutableMap.Builder<String, ConfigSku>()
                .put(SKU1, ImmutableConfigSku.builder()
                        .type(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .put(SKU2, ImmutableConfigSku.builder()
                        .type(TYPE)
                        .price(price)
                        .title(title)
                        .description(description)
                        .packageName(packageName)
                        .build())
                .build());
    }

    @Test
    public void testBundleOK() {
        when(apiOverrides.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_OK);

        Bundle bundle = testObject.newBuilder()
                .skus(ImmutableList.of(SKU1, SKU2), TYPE)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        ArrayList<String> detailsList =  bundle.getStringArrayList(GoogleUtil.DETAILS_LIST);
        try {
            JSONAssert.assertEquals(
                    "{\"type\":\"subs\",\"productId\":\"sku1\",\"price\":\"$1.98\"," +
                            "\"description\":\"some description\",\"title\":\"caps for sale\"," +
                            "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}",
                    detailsList.get(0),
                    JSONCompareMode.LENIENT);
            JSONAssert.assertEquals(
                    "{\"type\":\"subs\",\"productId\":\"sku2\",\"price\":\"$1.98\"," +
                            "\"description\":\"some description\",\"title\":\"caps for sale\"," +
                            "\"price_amount_micros\":1980000,\"price_currency_code\":\"USD\"}",
                    detailsList.get(1),
                    JSONCompareMode.LENIENT);
        } catch (JSONException e) {
           throw  Exceptions.propagate(e);
        }
    }

    @Test
    public void testBundleNotOK() {
        when(apiOverrides.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

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
        verify(apiOverrides).getGetSkuDetailsResponse();
    }
}

