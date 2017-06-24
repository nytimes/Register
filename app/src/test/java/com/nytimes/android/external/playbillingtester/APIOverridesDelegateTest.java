package com.nytimes.android.external.playbillingtester;

import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class APIOverridesDelegateTest {

    public static final int RESULT_DEFAULT = -1;            // - no user override

    private APIOverridesDelegate testObject;

    @Mock
    private APIOverrides apiOverrides;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObject = new APIOverridesDelegate(apiOverrides);
    }

    @Test
    public void setApiOverridesValueWithValidIdCallsApiOverrides() {
        ConfigResponse mockConfig = mock(ConfigResponse.class);
        when(mockConfig.responseCode()).thenReturn(-1);

        // Valid entries
        testObject.setApiOverridesValue(R.id.isBillingSupported, mockConfig);
        testObject.setApiOverridesValue(R.id.getBuyIntent, mockConfig);
        testObject.setApiOverridesValue(R.id.buy, mockConfig);
        testObject.setApiOverridesValue(R.id.getPurchases, mockConfig);
        testObject.setApiOverridesValue(R.id.getSkuDetails, mockConfig);
        testObject.setApiOverridesValue(R.id.getBuyIntentToReplaceSkus, mockConfig);

        // verify valid interactions
        verify(apiOverrides, times(1)).setIsBillingSupportedResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(1)).setGetBuyIntentResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(1)).setBuyResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(1)).setGetPurchasesResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(1)).setGetSkuDetailsResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(1)).setGetBuyIntentToReplaceSkusResponse(RESULT_DEFAULT);
    }

    @Test
    public void setApiOverridesValueWithInvalidIdCallsNothing() {
        ConfigResponse mockConfig = mock(ConfigResponse.class);
        when(mockConfig.responseCode()).thenReturn(-1);

        // Valid entries
        testObject.setApiOverridesValue(android.R.id.hint, mockConfig);
        testObject.setApiOverridesValue(android.R.id.home, mockConfig);
        testObject.setApiOverridesValue(android.R.id.redo, mockConfig);
        testObject.setApiOverridesValue(android.R.id.accessibilityActionContextClick, mockConfig);
        testObject.setApiOverridesValue(android.R.id.candidatesArea, mockConfig);
        testObject.setApiOverridesValue(android.R.id.keyboardView, mockConfig);

        // verify only valid interactions
        verify(apiOverrides, times(0)).setIsBillingSupportedResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(0)).setGetBuyIntentResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(0)).setBuyResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(0)).setGetPurchasesResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(0)).setGetSkuDetailsResponse(RESULT_DEFAULT);
        verify(apiOverrides, times(0)).setGetBuyIntentToReplaceSkusResponse(RESULT_DEFAULT);
    }

    @Test
    public void getApiOverridesValueWithValidIdCallsGetters() {
        ConfigResponse mockConfig = mock(ConfigResponse.class);
        when(mockConfig.responseCode()).thenReturn(-1);

        // Valid entries
        testObject.getApiOverridesValue(R.id.isBillingSupported);
        testObject.getApiOverridesValue(R.id.getBuyIntent);
        testObject.getApiOverridesValue(R.id.buy);
        testObject.getApiOverridesValue(R.id.getPurchases);
        testObject.getApiOverridesValue(R.id.getSkuDetails);
        testObject.getApiOverridesValue(R.id.getBuyIntentToReplaceSkus);

        // verify only valid interactions
        verify(apiOverrides, times(1)).getIsBillingSupportedResponse();
        verify(apiOverrides, times(1)).getGetBuyIntentResponse();
        verify(apiOverrides, times(1)).getBuyResponse();
        verify(apiOverrides, times(1)).getGetPurchasesResponse();
        verify(apiOverrides, times(1)).getGetSkuDetailsResponse();
        verify(apiOverrides, times(1)).getGetBuyIntentToReplaceSkusResponse();
    }

    @Test
    public void getApiOverridesValueWithInvalidIdReturnsNegativeOne() {
        ConfigResponse mockConfig = mock(ConfigResponse.class);
        when(mockConfig.responseCode()).thenReturn(-1);

        // Valid entries
        testObject.getApiOverridesValue(android.R.id.hint);
        testObject.getApiOverridesValue(android.R.id.home);
        testObject.getApiOverridesValue(android.R.id.redo);
        testObject.getApiOverridesValue(android.R.id.accessibilityActionContextClick);
        testObject.getApiOverridesValue(android.R.id.candidatesArea);
        testObject.getApiOverridesValue(android.R.id.keyboardView);

        // verify only valid interactions
        verify(apiOverrides, times(0)).getIsBillingSupportedResponse();
        verify(apiOverrides, times(0)).getGetBuyIntentResponse();
        verify(apiOverrides, times(0)).getBuyResponse();
        verify(apiOverrides, times(0)).getGetPurchasesResponse();
        verify(apiOverrides, times(0)).getGetSkuDetailsResponse();
        verify(apiOverrides, times(0)).getGetBuyIntentToReplaceSkusResponse();
    }

    @Test
    public void getApiOverridesValueWithInvalidIdReturnsCorrectValue() {
        when(apiOverrides.getIsBillingSupportedResponse()).thenReturn(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        int isBillingSupported = testObject.getApiOverridesValue(R.id.isBillingSupported);
        assertThat(isBillingSupported).isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE);

        when(apiOverrides.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_ITEM_NOT_OWNED);
        int getBuyIntent = testObject.getApiOverridesValue(R.id.getBuyIntent);
        assertThat(getBuyIntent).isEqualTo(GoogleUtil.RESULT_ITEM_NOT_OWNED);

        when(apiOverrides.getBuyResponse()).thenReturn(GoogleUtil.RESULT_DEVELOPER_ERROR);
        int buy = testObject.getApiOverridesValue(R.id.buy);
        assertThat(buy).isEqualTo(GoogleUtil.RESULT_DEVELOPER_ERROR);

        when(apiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_ERROR);
        int getPurchases = testObject.getApiOverridesValue(R.id.getPurchases);
        assertThat(getPurchases).isEqualTo(GoogleUtil.RESULT_ERROR);

        when(apiOverrides.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ITEM_ALREADY_OWNED);
        int getSkuDetails = testObject.getApiOverridesValue(R.id.getSkuDetails);
        assertThat(getSkuDetails).isEqualTo(GoogleUtil.RESULT_ITEM_ALREADY_OWNED);

        when(apiOverrides.getGetBuyIntentToReplaceSkusResponse()).thenReturn(GoogleUtil.RESULT_OK);
        int getBuyIntentToReplaceSkus = testObject.getApiOverridesValue(R.id.getBuyIntentToReplaceSkus);
        assertThat(getBuyIntentToReplaceSkus).isEqualTo(GoogleUtil.RESULT_OK);
    }
}
