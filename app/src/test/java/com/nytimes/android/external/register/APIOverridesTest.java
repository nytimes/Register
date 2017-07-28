package com.nytimes.android.external.register;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.nytimes.android.external.register.APIOverrides.PREF_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class APIOverridesTest {

    private static final int IS_BILLING_RESPONSE = -100;
    private static final int GET_BUY_INTENT_RESPONSE = -101;
    private static final int BUY_RESPONSE = -102;
    private static final int GET_PURCHASES_RESPONSE = -103;
    private static final int GET_SKU_DETAILS_RESPONSE = -104;
    private static final int CONSUME_PURCHASE_RESPONSE = -105;
    private static final String GET_USERS_RESPONSE = "myfun@user.com";
    private APIOverrides testObject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        SharedPreferences sharedPreferences =
                RuntimeEnvironment.application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        testObject = new APIOverrides(sharedPreferences);
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
    public void testConsumePurchaseResponse() {
        testObject.setConsumePurchaseResponse(CONSUME_PURCHASE_RESPONSE);
        assertThat(testObject.getConsumePurchaseResponse())
                .isEqualTo(CONSUME_PURCHASE_RESPONSE);
    }

    @Test
    public void testGetUsersResponse() {
        testObject.setUsersReponse(GET_USERS_RESPONSE);
        assertThat(testObject.getUsersResponse())
                .isEqualTo(GET_USERS_RESPONSE);
    }
}
