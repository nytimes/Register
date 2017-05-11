package com.nytimes.android.external.playbillingtester;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.KeyEvent;

import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.playbillingtester.di.Injector;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ConfigSku;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.Iterator;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Activity that displays dialog allowing user to purchase item
 */
@SuppressWarnings("PMD.UseVarargs")
public class BuyActivity extends AppCompatActivity {
    static final String RECEIPT_FMT = "%s.playBillingTesterToken%d";
    static final String TITLE_FMT =  "%s($%s)";
    static final String ERROR_FMT =  "%s: %d";

    @Inject
    protected APIOverridesAndPurchases apiOverridesAndPurchases;
    @Inject
    protected Gson gson;
    @Inject
    protected Config config;
    @Inject
    AlertDialog.Builder dialogBuilder;

    String sku;
    String itemtype;
    String developerPayload;
    long currentTimeMillis;

    DialogInterface.OnClickListener handleBuy = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String newReceipt = String.format(Locale.getDefault(), RECEIPT_FMT,
                    apiOverridesAndPurchases.getUsersResponse(), currentTimeMillis);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_OK);
            InAppPurchaseData inAppPurchaseData = new InAppPurchaseData.Builder()
                    .orderId(Long.toString(currentTimeMillis))
                    .packageName(config.skus().get(sku).packageName())
                    .productId(sku)
                    .purchaseTime(Long.toString(currentTimeMillis))
                    .developerPayload(developerPayload)
                    .purchaseToken(newReceipt)
                    .build();
            String inAppPurchaseDataStr = gson.toJson(inAppPurchaseData);
            apiOverridesAndPurchases.addPurchase(inAppPurchaseDataStr, itemtype);
            resultIntent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, inAppPurchaseDataStr);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

    DialogInterface.OnClickListener handleAlreadyOwned = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Iterator iterator = apiOverridesAndPurchases.getInAppPurchaseData(itemtype).iterator();
            if (iterator.hasNext()) {
                Intent intent = new Intent();
                intent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_ITEM_ALREADY_OWNED);
                intent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, gson.toJson(iterator.next()));
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    };

    DialogInterface.OnKeyListener handleKey = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                setResult(RESULT_OK);
                dialog.dismiss();
                finish();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_buy);

        sku = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_SKU);
        itemtype = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE);
        developerPayload = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showBuyDialog(getBuyResponse());
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    void showBuyDialog(int buyResponse) {
        Pair<String, String> titleAndMessage = getDialogTitleAndMessage(buyResponse);
        currentTimeMillis = System.currentTimeMillis();
        dialogBuilder
                .setTitle(titleAndMessage.first)
                .setMessage(titleAndMessage.second)
                .setOnKeyListener(handleKey);
        if (buyResponse == GoogleUtil.RESULT_OK) {
            dialogBuilder.setPositiveButton(R.string.buy, handleBuy);
        } else if (buyResponse == GoogleUtil.RESULT_ITEM_ALREADY_OWNED) {
            dialogBuilder.setPositiveButton(R.string.ok, handleAlreadyOwned);
        }
        dialogBuilder.create().show();
    }

    private Pair<String, String> getDialogTitleAndMessage(int buyResponse) {
        String title, message;
        switch (buyResponse) {
            case GoogleUtil.RESULT_OK:
                ConfigSku configSku = config.skus().get(sku);
                title = String.format(TITLE_FMT, configSku.title(), configSku.price());
                message = configSku.description();
                break;
            case GoogleUtil.RESULT_ITEM_UNAVAILABLE:
                title = getString(R.string.error);
                message = getString(R.string.item_not_found);
                break;
            case GoogleUtil.RESULT_ITEM_ALREADY_OWNED:
                title = getString(R.string.error);
                message = getString(R.string.item_already_owned);
                break;
            case GoogleUtil.RESULT_DEVELOPER_ERROR:
            case GoogleUtil.RESULT_ERROR:
            default:
                title = getString(R.string.error);
                message = String.format(Locale.getDefault(), ERROR_FMT, title, buyResponse);
                break;
        }
        return Pair.create(title, message);
    }

    int getBuyResponse() {
        int response = apiOverridesAndPurchases.getBuyResponse();
        if (response == APIOverridesAndPurchases.RESULT_DEFAULT) {
            if (config.skus().get(sku) == null) {
                response = GoogleUtil.RESULT_ITEM_UNAVAILABLE;
            } else if (apiOverridesAndPurchases.getReceiptForSku(sku, itemtype).isPresent()) {
                response = GoogleUtil.RESULT_ITEM_ALREADY_OWNED;
            } else {
                response = GoogleUtil.RESULT_OK;
            }
        }
        return response;
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        bundle.putInt(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_USER_CANCELED);
        setResult(RESULT_OK);
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionHandler.handlePermissionResult(requestCode, this, grantResults);
    }
}
