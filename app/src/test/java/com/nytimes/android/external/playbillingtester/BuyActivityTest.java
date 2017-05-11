package com.nytimes.android.external.playbillingtester;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.view.KeyEvent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.playbillingtester.di.GsonFactory;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ConfigSku;
import com.nytimes.android.external.playbillingtester.model.ImmutableConfigSku;
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
import org.robolectric.shadows.ShadowActivity;

import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.nytimes.android.external.playbillingtester.BuyActivity.ERROR_FMT;
import static com.nytimes.android.external.playbillingtester.BuyActivity.RECEIPT_FMT;
import static com.nytimes.android.external.playbillingtester.BuyActivity.TITLE_FMT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class)
public class BuyActivityTest {

    private static final String SKU = "sku1";
    private static final String TYPE = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;
    private static final String DEVELOPER_PAYLOAD = "devPayload";
    private static final String DESCRIPTION = "some description";
    private static final String PACKAGE_NAME = "com.my.pkg";
    private static final String PRICE = "1.98";
    private static final String TITLE = "caps for sale";
    private static final String USER = "myfun@user.com";
    private static final long CURRENT_TIME_MS = 1234567L;

    private BuyActivity testObject;
    private ShadowActivity shadowActivity;
    private final ConfigSku configSku = ImmutableConfigSku.builder()
            .description(DESCRIPTION)
            .itemType(TYPE)
            .packageName(PACKAGE_NAME)
            .price(PRICE)
            .title(TITLE)
            .build();
    private ImmutableMap.Builder<String, ConfigSku> configSkuMapBuilder;
    private final InAppPurchaseData inAppPurchaseData = new InAppPurchaseData.Builder()
            .orderId(Long.toString(CURRENT_TIME_MS))
            .packageName(PACKAGE_NAME)
            .productId(SKU)
            .purchaseTime(Long.toString(CURRENT_TIME_MS))
            .developerPayload(DEVELOPER_PAYLOAD)
            .purchaseToken(String.format(Locale.getDefault(), RECEIPT_FMT, USER, CURRENT_TIME_MS))
            .build();

    private ActivityController controller;
    private Gson gson = GsonFactory.create();

    @Mock
    private APIOverridesAndPurchases apiOverridesAndPurchases;
    @Mock
    private Config config;
    @Mock
    private AlertDialog.Builder alertDialogBuilder;
    @Mock
    private AlertDialog alertDialog;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Intent intent = new Intent(RuntimeEnvironment.application, TestBuyActivity.class);
        intent.putExtra(BuyIntentBundleBuilder.EX_SKU, SKU);
        intent.putExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE, TYPE);
        intent.putExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD, DEVELOPER_PAYLOAD);

        controller = Robolectric.buildActivity(TestBuyActivity.class, intent).create();
        testObject = (BuyActivity) controller.get();
        testObject.apiOverridesAndPurchases = apiOverridesAndPurchases;
        testObject.gson = gson;
        testObject.config = config;
        testObject.dialogBuilder = alertDialogBuilder;
        shadowActivity = shadowOf(testObject);

        when(alertDialogBuilder.setTitle(anyString())).thenReturn(alertDialogBuilder);
        when(alertDialogBuilder.setMessage(anyString())).thenReturn(alertDialogBuilder);
        when(alertDialogBuilder.setOnKeyListener(any(Dialog.OnKeyListener.class))).thenReturn(alertDialogBuilder);
        when(alertDialogBuilder.create()).thenReturn(alertDialog);

        configSkuMapBuilder = ImmutableMap.builder();
    }

    @Test
    public void testDialogBuyResponseOK() {
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(APIOverridesAndPurchases.RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getReceiptForSku(SKU, TYPE)).thenReturn(Optional.<String>absent());
        configSkuMapBuilder.put(SKU, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();

        verify(testObject.dialogBuilder).setTitle(String.format(Locale.getDefault(), TITLE_FMT, TITLE, PRICE));
        verify(testObject.dialogBuilder).setMessage(DESCRIPTION);
        verify(testObject.dialogBuilder).setOnKeyListener(testObject.handleKey);
        verify(testObject.dialogBuilder).setPositiveButton(R.string.buy, testObject.handleBuy);
    }

    @Test
    public void testDialogItemUnavailable() {
        String sku2 = "sku2";
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(APIOverridesAndPurchases.RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getReceiptForSku(SKU, TYPE)).thenReturn(Optional.<String>absent());
        configSkuMapBuilder.put(sku2, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();

        verify(testObject.dialogBuilder).setTitle(getStringResource(R.string.error));
        verify(testObject.dialogBuilder).setMessage(getStringResource(R.string.item_not_found));
        verify(testObject.dialogBuilder).setOnKeyListener(testObject.handleKey);
        verify(testObject.dialogBuilder, never()).setPositiveButton(anyString(), any(Dialog.OnClickListener.class));
    }

    @Test
    public void testDialogItemAlreadyOwned() {
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(APIOverridesAndPurchases.RESULT_DEFAULT);
        when(apiOverridesAndPurchases.getReceiptForSku(SKU, TYPE)).thenReturn(Optional.of(SKU));
        configSkuMapBuilder.put(SKU, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();

        verify(testObject.dialogBuilder).setTitle(getStringResource(R.string.error));
        verify(testObject.dialogBuilder).setMessage(getStringResource(R.string.item_already_owned));
        verify(testObject.dialogBuilder).setOnKeyListener(testObject.handleKey);
        verify(testObject.dialogBuilder).setPositiveButton(R.string.ok, testObject.handleAlreadyOwned);
    }

    @Test
    public void testDialogError() {
        int resultCode = GoogleUtil.RESULT_ERROR;
        when(apiOverridesAndPurchases.getBuyResponse()).thenReturn(resultCode);

        controller.start();

        verify(testObject.dialogBuilder).setTitle(getStringResource(R.string.error));
        verify(testObject.dialogBuilder).setMessage(String.format(Locale.getDefault(), ERROR_FMT,
                getStringResource(R.string.error), resultCode));
        verify(testObject.dialogBuilder).setOnKeyListener(testObject.handleKey);
        verify(testObject.dialogBuilder, never()).setPositiveButton(anyString(), any(Dialog.OnClickListener.class));
    }

    @Test
    public void testHandleBuy() {
        when(apiOverridesAndPurchases.getUsersResponse()).thenReturn(USER);
        configSkuMapBuilder.put(SKU, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();
        testObject.currentTimeMillis = CURRENT_TIME_MS;
        testObject.handleBuy.onClick(mock(Dialog.class), 0);

        assertThat(shadowActivity.getResultCode())
                .isEqualTo(RESULT_OK);
        Intent resultIntent = shadowActivity.getResultIntent();
        assertThat(resultIntent.getIntExtra(GoogleUtil.RESPONSE_CODE, APIOverridesAndPurchases.RESULT_DEFAULT))
                .isEqualTo(GoogleUtil.RESULT_OK);
        String inAppPurchaseDataStr = gson.toJson(inAppPurchaseData);
        assertThat(inAppPurchaseDataStr)
                .isEqualTo(resultIntent.getStringExtra(GoogleUtil.INAPP_PURCHASE_DATA));
        verify(apiOverridesAndPurchases).addPurchase(inAppPurchaseDataStr, TYPE);
    }

    @Test
    public void testHandleAlreadyOwnedValid() {
        when(apiOverridesAndPurchases.getInAppPurchaseData(TYPE)).thenReturn(ImmutableSet.of(inAppPurchaseData));
        configSkuMapBuilder.put(SKU, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();
        testObject.handleAlreadyOwned.onClick(mock(Dialog.class), 0);

        assertThat(shadowActivity.getResultCode())
                .isEqualTo(RESULT_OK);
        Intent resultIntent = shadowActivity.getResultIntent();
        assertThat(resultIntent.getIntExtra(GoogleUtil.RESPONSE_CODE, -1))
                .isEqualTo(GoogleUtil.RESULT_ITEM_ALREADY_OWNED);
    }

    @Test
    public void testHandleAlreadyOwnedInvalid() {
        when(apiOverridesAndPurchases.getInAppPurchaseData(TYPE)).thenReturn(ImmutableSet.<InAppPurchaseData>of());
        configSkuMapBuilder.put(SKU, configSku);
        when(config.skus()).thenReturn(configSkuMapBuilder.build());

        controller.start();
        testObject.handleAlreadyOwned.onClick(mock(Dialog.class), 0);

        assertThat(shadowActivity.getResultCode())
                .isEqualTo(RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent())
                .isNull();
    }

    @Test
    public void testHandleKeyBack() {
        boolean handled = testObject.handleKey.onKey(mock(Dialog.class), KeyEvent.KEYCODE_BACK, mock(KeyEvent.class));
        assertThat(handled)
                .isTrue();
        assertThat(shadowActivity.getResultCode())
                .isEqualTo(RESULT_OK);
    }
    @Test
    public void testHandleKeyNotBack() {
        boolean handled = testObject.handleKey.onKey(mock(Dialog.class), KeyEvent.KEYCODE_0, mock(KeyEvent.class));
        assertThat(handled)
                .isFalse();
    }

    private String getStringResource(int id) {
        return RuntimeEnvironment.application.getResources().getString(id);
    }

    static class TestBuyActivity extends BuyActivity {
        @Override
        protected void inject() {
            //intentionally blank
        }
    }
}
