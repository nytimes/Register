package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.google.common.collect.ImmutableSet;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLooper;

import java.util.Locale;

import static com.nytimes.android.external.playbillingtester.APIOverrides.RESULT_DEFAULT;
import static com.nytimes.android.external.playbillingtester.BuyActivity.RECEIPT_FMT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class, sdk = 21)
@SuppressWarnings("PMD.MethodNamingConventions")
public class MainActivityTest {

    private static final String DEVELOPER_PAYLOAD = "devPayload";
    private static final String packageName = "com.my.pkg";
    private static final String USER1 = "myfun@user.com";
    private static final String USER2 = "myfun2@user.com";
    private static final long CURRENT_TIME_MS = 1234567L;
    private static final String SKU1 = "sku1";
    private static final String SKU2 = "sku2";

    private MainActivity testObject;
    private ActivityController controller;
    private ShadowActivity shadowMain;

    @Mock
    private Purchases purchases;
    @Mock
    private APIOverrides mockApiOverrides;

    private final InAppPurchaseData inAppPurchaseData1 = new InAppPurchaseData.Builder()
            .orderId(Long.toString(CURRENT_TIME_MS))
            .packageName(packageName)
            .productId(SKU1)
            .purchaseTime(Long.toString(CURRENT_TIME_MS))
            .developerPayload(DEVELOPER_PAYLOAD)
            .purchaseToken(String.format(Locale.getDefault(), RECEIPT_FMT, USER1, CURRENT_TIME_MS))
            .build();
    private final InAppPurchaseData inAppPurchaseData2 = new InAppPurchaseData.Builder()
            .orderId(Long.toString(CURRENT_TIME_MS))
            .packageName(packageName)
            .productId(SKU2)
            .purchaseTime(Long.toString(CURRENT_TIME_MS))
            .developerPayload(DEVELOPER_PAYLOAD)
            .purchaseToken(String.format(Locale.getDefault(), RECEIPT_FMT, USER2, CURRENT_TIME_MS))
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(MainActivityTest.TestMainActivity.class).create();
        testObject = (MainActivity) controller.get();
        testObject.apiOverrides = mockApiOverrides;
        testObject.purchases = purchases;
        shadowMain = Shadow.extract(testObject);
    }

    @Test
    public void updatePurchases_whenHas_showsItems() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1));  // init
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of(inAppPurchaseData2));  // init

        RecyclerView list = (RecyclerView) testObject.findViewById(R.id.list);
        View emptyView = testObject.findViewById(R.id.empty_view);

        // Verify empty
        controller.start().postResume();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(2);
        assertThat(emptyView.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void updatePurchases_whenNone_showsEmpty() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of());  // init
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of());   // init

        RecyclerView list = (RecyclerView) testObject.findViewById(R.id.list);
        View emptyView = testObject.findViewById(R.id.empty_view);

        // Verify empty
        controller.start().postResume();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(0);
        assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onMenuClick_whenDeleteAll_showsNoItems() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1)) // Initial
                .thenReturn(ImmutableSet.of()); // After purge

        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.menu_action_delete_all);

        RecyclerView list = (RecyclerView) testObject.findViewById(R.id.list);

        // Start and make sure we have 1 IAP
        controller.start().postResume();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(1);

        // Purge and make sure we are empty
        testObject.onOptionsItemSelected(item);
        verify(purchases).purgePurchases();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(0);
    }

    @Test
    public void onMenuClick_whenRefresh_refreshContent() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of())   // init
                .thenReturn(ImmutableSet.of(inAppPurchaseData1));   // after refresh
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of())   // init
                .thenReturn(ImmutableSet.of(inAppPurchaseData2));   // after refresh

        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.menu_action_refresh);

        RecyclerView list = (RecyclerView) testObject.findViewById(R.id.list);

        // Verify empty
        controller.start().postResume();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(0);

        //Refresh
        testObject.onOptionsItemSelected(item);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(2);
    }

    @Test
    public void onMenuClick_whenSettings_startsSettings() {
        // Setup
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.menu_action_settings);

        controller.start();
        testObject.onOptionsItemSelected(item);

        // Verify SettingsActivity started
        Intent startedIntent = shadowMain.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getIntentClass()).isEqualTo(SettingsActivity.class);
    }

    @Test
    public void onCreate_whenDefaultValues_showsDefaultValues() {
        when(mockApiOverrides.getIsBillingSupportedResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetBuyIntentResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getBuyResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetPurchasesResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetSkuDetailsResponse()).thenReturn(RESULT_DEFAULT);;
        when(mockApiOverrides.getGetBuyIntentToReplaceSkusResponse()).thenReturn(RESULT_DEFAULT);

        controller.postCreate(null).start();

        assertThat(getSpinnerValue(R.id.isBillingSupported)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getBuyIntent)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.buy)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getPurchases)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getSkuDetails)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getBuyIntentToReplaceSkus)).isEqualTo(RESULT_DEFAULT);
    }

    @Test
    public void onCreate_whenUniqueValues_showsUniqueValues() {
        when(mockApiOverrides.getIsBillingSupportedResponse())
                .thenReturn(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        when(mockApiOverrides.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(mockApiOverrides.getBuyResponse()).thenReturn(GoogleUtil.RESULT_ITEM_UNAVAILABLE);
        when(mockApiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_DEVELOPER_ERROR);
        when(mockApiOverrides.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ERROR);
        when(mockApiOverrides.getGetBuyIntentToReplaceSkusResponse()).thenReturn(GoogleUtil.RESULT_ITEM_NOT_OWNED);

        controller.postCreate(null).start();

        assertThat(getSpinnerValue(R.id.isBillingSupported)).isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        assertThat(getSpinnerValue(R.id.getBuyIntent)).isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(getSpinnerValue(R.id.buy)).isEqualTo(GoogleUtil.RESULT_ITEM_UNAVAILABLE);
        assertThat(getSpinnerValue(R.id.getPurchases)).isEqualTo(GoogleUtil.RESULT_DEVELOPER_ERROR);
        assertThat(getSpinnerValue(R.id.getSkuDetails)).isEqualTo(GoogleUtil.RESULT_ERROR);
        assertThat(getSpinnerValue(R.id.getBuyIntentToReplaceSkus)).isEqualTo(GoogleUtil.RESULT_ITEM_NOT_OWNED);
    }

    @Test
    public void onItemSelected_whenChanged_setsApiOverrides() {
        controller.postCreate(null).start();

        AdapterView<Adapter> isBillingSupportedSpinner = getInternalSpinner(R.id.isBillingSupported);
        AdapterView<Adapter> getBuyIntentSpinner = getInternalSpinner(R.id.getBuyIntent);
        AdapterView<Adapter> buySpinner = getInternalSpinner(R.id.buy);
        AdapterView<Adapter> getPurchasesSpinner = getInternalSpinner(R.id.getPurchases);
        AdapterView<Adapter> getSkuDetailsSpinner = getInternalSpinner(R.id.getSkuDetails);
        AdapterView<Adapter> getGetBuyIntentToReplaceSkusResponseSpinner = getInternalSpinner(R.id.getBuyIntentToReplaceSkus);

        assertThat(isBillingSupportedSpinner.getTag()).isNull();
        assertThat(getBuyIntentSpinner.getTag()).isNull();
        assertThat(buySpinner.getTag()).isNull();
        assertThat(getPurchasesSpinner.getTag()).isNull();
        assertThat(getSkuDetailsSpinner.getTag()).isNull();
        assertThat(getGetBuyIntentToReplaceSkusResponseSpinner.getTag()).isNull();

        AdapterView parentView = mock(AdapterView.class);
        View view = mock(View.class);

        isBillingSupportedSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 0, 0L);
        verify(mockApiOverrides).setIsBillingSupportedResponse(RESULT_DEFAULT);

        getBuyIntentSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 1, 0L);
        verify(mockApiOverrides).setGetBuyIntentResponse(GoogleUtil.RESULT_OK);

        buySpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 2, 0L);
        verify(mockApiOverrides).setBuyResponse(GoogleUtil.RESULT_ITEM_UNAVAILABLE);

        getPurchasesSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 3, 0L);
        verify(mockApiOverrides).setGetPurchasesResponse(GoogleUtil.RESULT_ERROR);

        getSkuDetailsSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 4, 0L);
        verify(mockApiOverrides).setGetSkuDetailsResponse(GoogleUtil.RESULT_ERROR);

        getGetBuyIntentToReplaceSkusResponseSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 5, 0L);
        verify(mockApiOverrides).setGetBuyIntentToReplaceSkusResponse(GoogleUtil.RESULT_ITEM_ALREADY_OWNED);
    }

    public long getSpinnerValue(int parentId) {
        return getInternalSpinner(parentId).getSelectedItemId();
    }

    private AdapterView<Adapter> getInternalSpinner(int parentId) {
        return (AdapterView<Adapter>) (testObject.findViewById(parentId)
                .findViewById(R.id.config_spinner));
    }

    static class TestMainActivity extends MainActivity {
        @Override
        protected void inject() {
            //intentionally blank
        }
    }
}
