package com.nytimes.android.external.playbillingtester;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.Set;

import static com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases.PREF_NAME;
import static com.nytimes.android.external.playbillingtester.di.GsonFactory.create;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class APIOverridesAndPurchasesTest extends TestCase {

    private static final int IS_BILLING_RESPONSE = -100;
    private static final int GET_BUY_INTENT_RESPONSE = -101;
    private static final int BUY_RESPONSE = -102;
    private static final int GET_PURCHASES_RESPONSE = -103;
    private static final int GET_SKU_DETAILS_RESPONSE = -104;
    private static final String GET_USERS_RESPONSE = "myfun@user.com";
    private static final String orderId1 = "order1";
    private static final String orderId2 = "order2";
    private static final String package1 = "package1";
    private static final String package2 = "package2";
    private static final String productId1 = "product1";
    private static final String productId2 = "product2";
    private static final String purchaseTime1 = "purchaseTime1";
    private static final String purchaseTime2 = "purchaseTime2";
    private static final String purchaseState1 = "purchaseState1";
    private static final String purchaseState2 = "purchaseState2";
    private static final String devPayload1 = "devPayload1";
    private static final String devPayload2 = "devPayload2";
    private static final String purchaseToken1 = "purchaseToken1";
    private static final String purchaseToken2 = "purchaseToken2";

    private APIOverridesAndPurchases testObject;
    private InAppPurchaseData inAppPurchaseData1, inAppPurchaseData2;
    private String inAppPurchaseData1Str, inAppPurchaseData2Str;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        Gson gson = create();

        SharedPreferences sharedPreferences =
                RuntimeEnvironment.application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        inAppPurchaseData1 = new InAppPurchaseData.Builder()
                .orderId(orderId1)
                .packageName(package1)
                .productId(productId1)
                .purchaseTime(purchaseTime1)
                .purchaseState(purchaseState1)
                .developerPayload(devPayload1)
                .purchaseToken(purchaseToken1)
                .build();

        inAppPurchaseData2 = new InAppPurchaseData.Builder()
                .orderId(orderId2)
                .packageName(package2)
                .productId(productId2)
                .purchaseTime(purchaseTime2)
                .purchaseState(purchaseState2)
                .developerPayload(devPayload2)
                .purchaseToken(purchaseToken2)
                .build();

        inAppPurchaseData1Str = gson.toJson(inAppPurchaseData1);
        inAppPurchaseData2Str = gson.toJson(inAppPurchaseData2);

        testObject = new APIOverridesAndPurchases(sharedPreferences, gson);
    }

    @Test
    public void testIsBillingSupported() {
        testObject.setIsBillingSupportedResponse(IS_BILLING_RESPONSE);
        assertThat(testObject.getIsBillingSupportedResponse())
                .isEqualTo(IS_BILLING_RESPONSE);
    }

    @Test
    public void testBuyIntentResponse() {
        testObject.setGetBuyIntentResponse(GET_BUY_INTENT_RESPONSE);
        assertThat(testObject.getGetBuyIntentResponse())
                .isEqualTo(GET_BUY_INTENT_RESPONSE);
    }

    @Test
    public void testBuyResponse() {
        testObject.setBuyResponse(BUY_RESPONSE);
        assertThat(testObject.getBuyResponse())
                .isEqualTo(BUY_RESPONSE);
    }

    @Test
    public void testPurchasesResponse() {
        testObject.setGetPurchasesResponse(GET_PURCHASES_RESPONSE);
        assertThat(testObject.getGetPurchasesResponse())
                .isEqualTo(GET_PURCHASES_RESPONSE);
    }

    @Test
    public void testGetSkuDetailsResponse() {
        testObject.setGetSkuDetailsResponse(GET_SKU_DETAILS_RESPONSE);
        assertThat(testObject.getGetSkuDetailsResponse())
                .isEqualTo(GET_SKU_DETAILS_RESPONSE);
    }

    @Test
    public void testGetUsersResponse() {
        testObject.setUsersReponse(GET_USERS_RESPONSE);
        assertThat(testObject.getUsersResponse())
                .isEqualTo(GET_USERS_RESPONSE);
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
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        List<String> subList = testObject.getInAppPurchaseDataAsArrayList(GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        List<String> iapList = testObject.getInAppPurchaseDataAsArrayList(GoogleUtil.BILLING_TYPE_IAP);
        assertThat(subList.get(0))
                .isEqualTo(inAppPurchaseData1Str);
        assertThat(iapList.get(0))
                .isEqualTo(inAppPurchaseData2Str);
    }

    @Test
    public void testGetReceiptForSkuFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Optional<String> actual = testObject.getReceiptForSku(productId1, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        assertThat(actual)
                .isEqualTo(Optional.of(purchaseToken1));
    }

    @Test
    public void testGetReceiptForSkuNotFound() {
        testObject.addPurchase(inAppPurchaseData1Str, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        testObject.addPurchase(inAppPurchaseData2Str, GoogleUtil.BILLING_TYPE_IAP);
        Optional<String> actual = testObject.getReceiptForSku(productId1, GoogleUtil.BILLING_TYPE_IAP);
        assertThat(actual)
                .isEqualTo(Optional.absent());
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
