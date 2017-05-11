package com.nytimes.android.external.playbillingtester;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import java.util.Locale;

import static com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases.RESULT_DEFAULT;
import static com.nytimes.android.external.playbillingtester.BuyActivity.RECEIPT_FMT;
import static com.nytimes.android.external.playbillingtester.MainActivity.VERSION_FMT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class)
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
    @Mock
    private APIOverridesAndPurchases apiOverridesAndPurchases;
    @Mock
    private Config config;
    @Mock
    private AlertDialog.Builder dialogBuilder;
    private Resources resources;
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
            .purchaseToken(String.format(Locale.getDefault(), RECEIPT_FMT, USER1, CURRENT_TIME_MS))
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        resources = RuntimeEnvironment.application.getResources();
        controller = Robolectric.buildActivity(MainActivityTest.TestMainActivity.class).create();
        testObject = (MainActivity) controller.get();
        testObject.apiOverridesAndPurchases = apiOverridesAndPurchases;
        testObject.config = config;
        testObject.dialogBuilder = dialogBuilder;
        when(config.users()).thenReturn(ImmutableList.of(USER1, USER2));
        when(dialogBuilder.setTitle(anyInt())).thenReturn(dialogBuilder);
        when(dialogBuilder.setMessage(anyString())).thenReturn(dialogBuilder);
    }

    @Test
    public void testDefaults() {
        when(apiOverridesAndPurchases.getIsBillingSupportedResponse()).thenReturn(RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getGetBuyIntentResponse()).thenReturn(RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getGetPurchasesResponse()).thenReturn(RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getGetSkuDetailsResponse()).thenReturn(RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getUsersResponse()).thenReturn(USER1);

        controller.start();

        String spnDefault = resources.getString(R.string.spn_default);
        assertThat(testObject.isBillingSupportedSpinner.getSelectedItem())
                .isEqualTo(spnDefault);
        assertThat(testObject.getBuyIntentSpinner.getSelectedItem())
                .isEqualTo(spnDefault);
        assertThat(testObject.buySpinner.getSelectedItem())
                .isEqualTo(spnDefault);
        assertThat(testObject.getPurchasesSpinner.getSelectedItem())
                .isEqualTo(spnDefault);
        assertThat(testObject.getSkuDetailsSpinner.getSelectedItem())
                .isEqualTo(spnDefault);
        assertThat(testObject.usersSpinner.getSelectedItem())
                .isEqualTo(USER1);
        assertThat(testObject.itemsTextView.getText().toString())
                .isEmpty();
    }

    @Test
    public void testNonDefaults() {
        when(apiOverridesAndPurchases.getIsBillingSupportedResponse())
                .thenReturn(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        when(apiOverridesAndPurchases.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(GoogleUtil.RESULT_ITEM_UNAVAILABLE);
        when(apiOverridesAndPurchases.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_DEVELOPER_ERROR);
        when(apiOverridesAndPurchases.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ERROR);
        when(apiOverridesAndPurchases.getUsersResponse()).thenReturn(USER2);
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1));
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of(inAppPurchaseData2));

        controller.start();

        assertThat(testObject.isBillingSupportedSpinner.getSelectedItem())
                .isEqualTo(resources.getString(R.string.spn_billing_unavailable));
        assertThat(testObject.getBuyIntentSpinner.getSelectedItem())
                .isEqualTo(resources.getString(R.string.spn_ok));
        assertThat(testObject.buySpinner.getSelectedItem())
                .isEqualTo(resources.getString(R.string.spn_item_unavailable));
        assertThat(testObject.getPurchasesSpinner.getSelectedItem())
                .isEqualTo(resources.getString(R.string.spn_dev_error));
        assertThat(testObject.getSkuDetailsSpinner.getSelectedItem())
                .isEqualTo(resources.getString(R.string.spn_error));
        assertThat(testObject.usersSpinner.getSelectedItem())
                .isEqualTo(USER2);
        assertThat(testObject.itemsTextView.getText().toString())
                .isEqualTo(inAppPurchaseData2.productId() + ";" + inAppPurchaseData2.purchaseToken() + "\n" +
                        inAppPurchaseData1.productId() + ";" + inAppPurchaseData1.purchaseToken() + "\n");
    }

    @Test
    public void testHandlePurge() {
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1))    // init
                .thenReturn(ImmutableSet.<InAppPurchaseData>of());  // after purge
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.of(inAppPurchaseData2))    // init
                .thenReturn(ImmutableSet.<InAppPurchaseData>of());  // after purge

        controller.start();

        assertThat(testObject.itemsTextView.getText().toString())
                .isEqualTo(inAppPurchaseData2.productId() + ";" + inAppPurchaseData2.purchaseToken() + "\n" +
                        inAppPurchaseData1.productId() + ";" + inAppPurchaseData1.purchaseToken() + "\n");

        testObject.handlePurge(mock(View.class));

        verify(apiOverridesAndPurchases).purgePurchases();
        assertThat(testObject.itemsTextView.getText().toString())
                .isEmpty();
    }

    @Test
    public void testHandleRefresh() {
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP))
                .thenReturn(ImmutableSet.of(inAppPurchaseData1))    // init
                .thenReturn(ImmutableSet.of(inAppPurchaseData1));   // after refresh
        when(apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION))
                .thenReturn(ImmutableSet.<InAppPurchaseData>of())   // init
                .thenReturn(ImmutableSet.of(inAppPurchaseData2));   // after refresh

        controller.start();

        assertThat(testObject.itemsTextView.getText().toString())
                .isEqualTo(inAppPurchaseData1.productId() + ";" + inAppPurchaseData1.purchaseToken() + "\n");

        testObject.handleRefresh(mock(View.class));

        assertThat(testObject.itemsTextView.getText().toString())
                .isEqualTo(inAppPurchaseData2.productId() + ";" + inAppPurchaseData2.purchaseToken() + "\n" +
                        inAppPurchaseData1.productId() + ";" + inAppPurchaseData1.purchaseToken() + "\n");
    }

    @Test
    public void testOnItemSelected() {
        controller.start();

        assertThat(testObject.checkedMap.get(R.id.isBillingSupported))
                .isTrue();
        assertThat(testObject.checkedMap.get(R.id.getBuyIntent))
                .isTrue();
        assertThat(testObject.checkedMap.get(R.id.buy))
                .isTrue();
        assertThat(testObject.checkedMap.get(R.id.getPurchases))
                .isTrue();
        assertThat(testObject.checkedMap.get(R.id.getSkuDetails))
                .isTrue();
        assertThat(testObject.checkedMap.get(R.id.usersSpinner))
                .isTrue();

        AdapterView parentView = mock(AdapterView.class);
        View view = mock(View.class);

        when(parentView.getId()).thenReturn(R.id.isBillingSupported);
        testObject.onItemSelected(parentView, view, 0, 0L);
        verify(apiOverridesAndPurchases)
                .setIsBillingSupportedResponse(RESULT_DEFAULT);

        when(parentView.getId()).thenReturn(R.id.getBuyIntent);
        testObject.onItemSelected(parentView, view, 1, 0L);
        verify(apiOverridesAndPurchases).setGetBuyIntentResponse(GoogleUtil.RESULT_OK);

        when(parentView.getId()).thenReturn(R.id.buy);
        testObject.onItemSelected(parentView, view, 2, 0L);
        verify(apiOverridesAndPurchases).setBuyResponse(GoogleUtil.RESULT_ITEM_UNAVAILABLE);

        when(parentView.getId()).thenReturn(R.id.getPurchases);
        testObject.onItemSelected(parentView, view, 3, 0L);
        verify(apiOverridesAndPurchases).setGetPurchasesResponse(GoogleUtil.RESULT_ERROR);

        when(parentView.getId()).thenReturn(R.id.getSkuDetails);
        testObject.onItemSelected(parentView, view, 4, 0L);
        verify(apiOverridesAndPurchases).setGetSkuDetailsResponse(GoogleUtil.RESULT_ERROR);

        when(parentView.getId()).thenReturn(R.id.usersSpinner);
        testObject.onItemSelected(parentView, view, 1, 0L);
        verify(apiOverridesAndPurchases).setUsersReponse(USER2);
    }

    @Test
    public void testAbout() {
        controller.start();

        MenuItem menuItem = mock(MenuItem.class);
        when(menuItem.getItemId()).thenReturn(R.id.action_settings);

        testObject.onOptionsItemSelected(menuItem);

        verify(dialogBuilder).setTitle(R.string.app_name);
        verify(dialogBuilder).setMessage(String.format(Locale.getDefault(), VERSION_FMT, BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE));
        verify(dialogBuilder).show();
    }

    static class TestMainActivity extends MainActivity {
        @Override
        protected void inject() {
            //intentionally blank
        }
    }
}
