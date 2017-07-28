package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.playbillingtester.APIOverrides;
import com.nytimes.android.external.playbillingtester.BillingServiceStubImpl;
import com.nytimes.android.external.playbillingtester.Purchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ConsumePurchaseResponseTest {

    private static final int API_VERSION = 3;
    private static final String PACKAGE_NAME = "com.my.package";

    @Mock
    private APIOverrides apiOverrides;

    @Mock
    private Purchases purchases;

    @Mock
    private Purchases.PurchasesLists inAppPurchasesLists;

    @Mock
    private Purchases.PurchasesLists subscriptionsPurchasesLists;

    @Mock
    private BuyIntentBundleBuilder buyIntentBundleBuilder;

    @Mock
    private SkuDetailsBundleBuilder skuDetailsBundleBuilder;

    @Mock
    private PurchasesBundleBuilder purchasesBundleBuilder;

    @Mock
    private BuyIntentToReplaceSkusBundleBuilder buyIntentToReplaceSkusBundleBuilder;

    private BillingServiceStubImpl testObject;

    private final Bundle expected = new Bundle();
    private final String type = GoogleUtil.BILLING_TYPE_IAP;

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
        ConsumePurchaseResponse consumePurchaseResponse = new ConsumePurchaseResponse(apiOverrides, purchases);
        testObject = new BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, consumePurchaseResponse, buyIntentToReplaceSkusBundleBuilder);
    }

    @Test
    public void testConsumePurchaseIAP() {
        final List<String> testPurchases = ImmutableList.of("purchase1", "purchase2");
        final List<String> subscriptions = ImmutableList.of("subscription1", "subscription2");
        Bundle getPurchasesBundle = new Bundle();
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, new ArrayList<>(testPurchases));
        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null)).thenReturn(inAppPurchasesLists);
        when(inAppPurchasesLists.purchaseDataList()).thenReturn(testPurchases);
        when(purchases.removePurchase("purchase1", GoogleUtil.BILLING_TYPE_IAP)).thenReturn(true);

        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists);
        when(subscriptionsPurchasesLists.purchaseDataList()).thenReturn(subscriptions);

        when(purchasesBundleBuilder.build()).thenReturn(getPurchasesBundle);

        Bundle stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token");
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases);
        assertThat(stored).isEqualTo(getPurchasesBundle);

        String purchaseToken = testPurchases.get(0);
        int result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken);
        assertThat(result).isEqualTo(GoogleUtil.RESULT_OK);
    }

    @Test
    public void testConsumePurchaseSubscription() {
        final List<String> testPurchases = ImmutableList.of("purchase1", "purchase2");
        final List<String> subscriptions = ImmutableList.of("subscription1", "subscription2");
        Bundle getPurchasesBundle = new Bundle();
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, new ArrayList<>(testPurchases));
        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists);
        when(inAppPurchasesLists.purchaseDataList()).thenReturn(testPurchases);
        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists);
        when(subscriptionsPurchasesLists.purchaseDataList())
                .thenReturn(subscriptions);
        when(purchasesBundleBuilder.build())
                .thenReturn(getPurchasesBundle);

        Bundle stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token");
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases);
        assertThat(stored).isEqualTo(getPurchasesBundle);

        String purchaseToken = subscriptions.get(0);

        int result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken);
        assertThat(result).isEqualTo(GoogleUtil.RESULT_ERROR);
    }

    @Test
    public void testConsumePurchaseNotOwned() {
        final List<String> testPurchases = ImmutableList.of("purchase1", "purchase2");
        final List<String> subscriptions = ImmutableList.of("subscription1", "subscription2");
        Bundle getPurchasesBundle = new Bundle();
        getPurchasesBundle.putStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST, new ArrayList<>(testPurchases));
        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP, null))
                .thenReturn(inAppPurchasesLists);
        when(inAppPurchasesLists.purchaseDataList()).thenReturn(testPurchases);
        when(purchases.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, null))
                .thenReturn(subscriptionsPurchasesLists);
        when(subscriptionsPurchasesLists.purchaseDataList())
                .thenReturn(subscriptions);
        when(purchasesBundleBuilder.build())
                .thenReturn(getPurchasesBundle);

        Bundle stored = testObject.getPurchases(API_VERSION, PACKAGE_NAME, type, "token");
        assertThat(stored.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(stored.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(testPurchases);
        assertThat(stored).isEqualTo(getPurchasesBundle);

        String purchaseToken = "Not owned product";

        int result = testObject.consumePurchase(API_VERSION, PACKAGE_NAME, purchaseToken);
        assertThat(result).isEqualTo(GoogleUtil.RESULT_ITEM_NOT_OWNED);
    }

}
