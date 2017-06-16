package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

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
        shadowMain = Shadow.extract(testObject);
        testObject.purchases = purchases;
    }

    @Test
    public void updatePurchases_whenHas_showsItems() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1));  // init
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of(inAppPurchaseData2));  // init

        RecyclerView list = testObject.findViewById(R.id.list);
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

        RecyclerView list = testObject.findViewById(R.id.list);
        View emptyView = testObject.findViewById(R.id.empty_view);

        // Verify empty
        controller.start().postResume();
        assertThat(list.getAdapter().getItemCount()).isEqualTo(0);
        assertThat(emptyView.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void onMenuClick_whenConfigure_startsConfigure() {
        // Setup
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.menu_action_configure);

        controller.start();
        testObject.onOptionsItemSelected(item);

        // Verify ConfigureActivity started
        Intent startedIntent = shadowMain.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getIntentClass()).isEqualTo(ConfigActivity.class);
    }

    @Test
    public void onMenuClick_whenDeleteAll_showsNoItems() {
        // Setup
        when(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1)) // Initial
                .thenReturn(ImmutableSet.of()); // After purge

        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.menu_action_delete_all);

        RecyclerView list = testObject.findViewById(R.id.list);

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

        RecyclerView list = testObject.findViewById(R.id.list);

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

    static class TestMainActivity extends MainActivity {
        @Override
        protected void inject() {
            //intentionally blank
        }
    }
}
