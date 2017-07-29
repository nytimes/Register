package com.nytimes.android.external.register;

import android.os.Bundle;
import android.os.RemoteException;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder;
import com.nytimes.android.external.register.bundle.ConsumePurchaseResponse;
import com.nytimes.android.external.register.bundle.PurchasesBundleBuilder;
import com.nytimes.android.external.register.bundle.SkuDetailsBundleBuilder;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static com.nytimes.android.external.register.APIOverrides.RESULT_DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BillingServicesSubImplTest {

    private static final int API_VERSION = 3;
    private static final String PACKAGE_NAME = "com.my.package";
    private static final String DEVELOPER_PAYLOAD = "devPayload";
    private static final String SKU = "sku";

    @Mock
    private APIOverrides apiOverrides;

    @Mock
    private BuyIntentBundleBuilder buyIntentBundleBuilder;

    @Mock
    private SkuDetailsBundleBuilder skuDetailsBundleBuilder;

    @Mock
    private PurchasesBundleBuilder purchasesBundleBuilder;

    @Mock
    private BuyIntentToReplaceSkusBundleBuilder buyIntentToReplaceSkusBundleBuilder;

    private BillingServiceStubImpl testObject;

    private final String type = GoogleUtil.BILLING_TYPE_IAP;
    private final Bundle expected = new Bundle();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(skuDetailsBundleBuilder.newBuilder()).thenReturn(skuDetailsBundleBuilder);
        when(skuDetailsBundleBuilder.skus(anyListOf(String.class), anyString())).thenReturn(skuDetailsBundleBuilder);
        when(skuDetailsBundleBuilder.build()).thenReturn(expected);
        when(buyIntentBundleBuilder.newBuilder()).thenReturn(buyIntentBundleBuilder);
        when(buyIntentBundleBuilder.developerPayload(anyString())).thenReturn(buyIntentBundleBuilder);
        when(buyIntentBundleBuilder.packageName(anyString())).thenReturn(buyIntentBundleBuilder);
        when(buyIntentBundleBuilder.sku(anyString())).thenReturn(buyIntentBundleBuilder);
        when(buyIntentBundleBuilder.type(anyString())).thenReturn(buyIntentBundleBuilder);
        when(buyIntentBundleBuilder.build()).thenReturn(expected);
        when(purchasesBundleBuilder.newBuilder()).thenReturn(purchasesBundleBuilder);
        when(purchasesBundleBuilder.type(anyString())).thenReturn(purchasesBundleBuilder);
        when(purchasesBundleBuilder.continuationToken(anyString())).thenReturn(purchasesBundleBuilder);
        when(purchasesBundleBuilder.build()).thenReturn(expected);
        when(buyIntentToReplaceSkusBundleBuilder.newBuilder()).thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.developerPayload(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.packageName(anyString()))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.newSku(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.oldSkus(anyListOf(String.class)))
                .thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.type(anyString())).thenReturn(buyIntentToReplaceSkusBundleBuilder);
        when(buyIntentToReplaceSkusBundleBuilder.build()).thenReturn(expected);
        testObject = new BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, mock(ConsumePurchaseResponse.class), buyIntentToReplaceSkusBundleBuilder);
    }

    @Test
    public void testIsBillingSupportedDefault() {
        int expected = GoogleUtil.RESULT_OK;
        when(apiOverrides.getIsBillingSupportedResponse()).thenReturn(RESULT_DEFAULT);
        int actual = testObject.isBillingSupported(API_VERSION, PACKAGE_NAME, type);
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void testIsBillingSupportedNonDefault() {
        int expected = GoogleUtil.RESULT_BILLING_UNAVAILABLE;
        when(apiOverrides.getIsBillingSupportedResponse()).thenReturn(expected);
        int actual = testObject.isBillingSupported(API_VERSION, PACKAGE_NAME, type);
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void testGetSkuDetails() {
        Bundle skusBundle = new Bundle();
        skusBundle.putStringArrayList(GoogleUtil.ITEM_ID_LIST, new ArrayList<>());
        Bundle actual = testObject.getSkuDetails(API_VERSION, PACKAGE_NAME, type, skusBundle);
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void testGetBuyIntent() {
        Bundle actual = testObject.getBuyIntent(API_VERSION, SKU, PACKAGE_NAME, type, DEVELOPER_PAYLOAD);
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void testGetPurchases() {
        String continuationToken = "conti";
        Bundle actual = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, continuationToken);
        assertThat(actual)
                .isEqualTo(expected);
    }

    @Test
    public void testConsumePurchase() {
        String purchaseToken = "token";
        assertThat(testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken))
                .isEqualTo(GoogleUtil.RESULT_OK);
    }

    @Test
    public void testStub() throws RemoteException {
        assertThat(testObject.stub(API_VERSION, PACKAGE_NAME, type))
                .isEqualTo(0);
    }

    @Test
    public void testGetBuyIntentToReplaceSkus() throws RemoteException {
        String sku2 = "sku2";
        assertThat(testObject.getBuyIntentToReplaceSkus(API_VERSION, PACKAGE_NAME,
                ImmutableList.of(SKU), sku2, type, DEVELOPER_PAYLOAD))
                .isEqualTo(expected);
    }

    @Test
    public void testAsBinder() {
        assertThat(testObject.asBinder())
                .isEqualTo(testObject);
    }
}
