package com.nytimes.android.external.register;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.ImmutableSet;
import com.nytimes.android.external.registerlib.GoogleUtil;
import com.nytimes.android.external.registerlib.InAppPurchaseData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.Set;

import static com.nytimes.android.external.register.APIOverrides.PREF_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchasesTest {

    private static final String ORDER_ID_1 = "order1";
    private static final String ORDER_ID_2 = "order2";
    private static final String PACKAGE_1 = "package1";
    private static final String PACKAGE_2 = "package2";
    private static final String PRODUCT_ID_1 = "product1";
    private static final String PRODUCT_ID_2 = "product2";
    private static final String PURCHASE_TIME_1 = "purchaseTime1";
    private static final String PURCHASE_TIME_2 = "purchaseTime2";
    private static final String PURCHASE_STATE_1 = "purchaseState1";
    private static final String PURCHASE_STATE_2 = "purchaseState2";
    private static final String DEV_PAYLOAD_1 = "devPayload1";
    private static final String DEV_PAYLOAD_2 = "devPayload2";
    private static final String PURCHASE_TOKEN_1 = "purchaseToken1";
    private static final String PURCHASE_TOKEN_2 = "purchaseToken2";

    @Mock
    private Signer signer;

    private Purchases testObject;
    private InAppPurchaseData inAppPurchaseData1, inAppPurchaseData2;
    private String inAppPurchaseData1Str, inAppPurchaseData2Str;
    private String continuationToken;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        SharedPreferences sharedPreferences =
                RuntimeEnvironment.application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        inAppPurchaseData1 = new InAppPurchaseData.Builder()
                .orderId(ORDER_ID_1)
                .packageName(PACKAGE_1)
                .productId(PRODUCT_ID_1)
                .purchaseTime(PURCHASE_TIME_1)
                .purchaseState(PURCHASE_STATE_1)
                .developerPayload(DEV_PAYLOAD_1)
                .purchaseToken(PURCHASE_TOKEN_1)
                .build();

        inAppPurchaseData2 = new InAppPurchaseData.Builder()
                .orderId(ORDER_ID_2)
                .packageName(PACKAGE_2)
                .productId(PRODUCT_ID_2)
                .purchaseTime(PURCHASE_TIME_2)
                .purchaseState(PURCHASE_STATE_2)
                .developerPayload(DEV_PAYLOAD_2)
                .purchaseToken(PURCHASE_TOKEN_2)
                .build();

        inAppPurchaseData1Str = InAppPurchaseData.toJson(inAppPurchaseData1);
        inAppPurchaseData2Str = InAppPurchaseData.toJson(inAppPurchaseData2);

        testObject = new Purchases(sharedPreferences, signer);

        when(signer.signData(anyString())).thenReturn("signedData");
    }

    @Test
    public void testAddPurchaseAndGetInAppPurchaseData() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Set<InAppPurchaseData> iapdActual1 = testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        Set<InAppPurchaseData> iapdActual2 = testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP);
        assertThat(iapdActual1.iterator().next())
                .isEqualTo(inAppPurchaseData1);
        assertThat(iapdActual2.iterator().next())
                .isEqualTo(inAppPurchaseData2);
    }

    @Test
    public void testGetInAppPurchaseDataAsArrayList() {
        continuationToken = "";
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Purchases.PurchasesLists purchasesSub = testObject.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION,
                continuationToken);
        Purchases.PurchasesLists purchasesIAP = testObject.getPurchasesLists(GoogleUtil.BILLING_TYPE_IAP,
                continuationToken);
        List<String> subList = purchasesSub.purchaseDataList();
        List<String> iapList = purchasesIAP.purchaseDataList();
        assertThat(subList.get(0))
                .isEqualTo(inAppPurchaseData1Str);
        assertThat(iapList.get(0))
                .isEqualTo(inAppPurchaseData2Str);
        assertThat(purchasesSub.continuationToken()).isNull();
        assertThat(purchasesIAP.continuationToken()).isNull();
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Test
    public void testGetInAppPurchaseDataAsArrayListWithContinuationToken() {
        // add PAGE_LIMIT + OVER+LIMIT purchases
        int overLimit = 3;
        for (int i = 0; i < Purchases.PAGE_LIMIT + overLimit; i++) {
            InAppPurchaseData inAppPurchaseData = new InAppPurchaseData.Builder()
                    .orderId(ORDER_ID_1 + i)
                    .packageName(PACKAGE_1)
                    .productId(PRODUCT_ID_1)
                    .purchaseTime(PURCHASE_TIME_1)
                    .purchaseState(PURCHASE_STATE_1)
                    .developerPayload(DEV_PAYLOAD_1)
                    .purchaseToken(PURCHASE_TOKEN_1)
                    .build();
            String inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData);
            testObject.addPurchase(inAppPurchaseDataStr, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        }

        // get first page
        Purchases.PurchasesLists purchasesSub = testObject.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION,
                continuationToken);

        // check
        List<String> subList = purchasesSub.purchaseDataList();
        assertThat(subList.size()).isEqualTo(Purchases.PAGE_LIMIT);
        assertThat(purchasesSub.continuationToken())
                .isEqualTo(Integer.toString(Purchases.PAGE_LIMIT));

        // get second page
        purchasesSub = testObject.getPurchasesLists(GoogleUtil.BILLING_TYPE_SUBSCRIPTION,
                purchasesSub.continuationToken());

        // check
        subList = purchasesSub.purchaseDataList();
        assertThat(subList.size()).isEqualTo(overLimit);
        assertThat(purchasesSub.continuationToken()).isNull();
    }

    @Test
    public void testGetReceiptForSkuFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Set<String> actual = testObject.getReceiptsForSkus(ImmutableSet.of(PRODUCT_ID_1),
                GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        assertThat(actual)
                .isEqualTo(ImmutableSet.of(PURCHASE_TOKEN_1));
    }

    @Test
    public void testGetReceiptForSkuNotFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Set<String> actual = testObject.getReceiptsForSkus(ImmutableSet.of(PRODUCT_ID_1),
                GoogleUtil.BILLING_TYPE_IAP);
        assertThat(actual)
                .isEqualTo(ImmutableSet.of());
    }

    @Test
    public void testPurgePurchases() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        testObject.purgePurchases();
        assertThat(testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION).size())
                .isEqualTo(0);
        assertThat(testObject.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP).size())
                .isEqualTo(0);
    }
}
