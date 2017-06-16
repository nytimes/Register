package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentToReplaceSkusBundleBuilder;
import com.nytimes.android.external.playbillingtester.di.Injector;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ConfigSku;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Activity that displays dialog allowing user to purchase item
 */
@SuppressWarnings("PMD.GodClass")
public class BuyActivity extends AppCompatActivity {

    static final String RECEIPT_FMT = "%s.playBillingTesterToken%d";
    static final String ERROR_FMT =  "%s: %d";

    private static final String RESPONSE_EXTRA_TITLE = "RESPONSE_EXTRA_TITLE";
    private static final String RESPONSE_EXTRA_SUMMARY = "RESPONSE_EXTRA_SUMMARY";
    private static final String RESPONSE_EXTRA_PRICE = "RESPONSE_EXTRA_PRICE";
    private static final String RESPONSE_EXTRA_REPLACE_OLD_SKU = "RESPONSE_EXTRA_REPLACE_OLD_SKU";

    @Inject
    protected APIOverrides apiOverrides;
    @Inject
    protected Purchases purchases;
    @Inject
    protected Gson gson;
    @Inject
    protected Config config;

    long currentTimeMillis;
    private String sku, newSku;
    private List<String> oldSkus;
    private String itemtype;
    private String developerPayload;
    private boolean isReplace = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);
        initPurchaseData();
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initPurchaseData() {
        sku = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_SKU);
        if (sku == null) {
            newSku = getIntent().getStringExtra(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU);
            oldSkus = getIntent().getStringArrayListExtra(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS);
            if (oldSkus == null) {
                sku = newSku;
            } else {
                isReplace = true;
            }
        }

        itemtype = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE);
        developerPayload = getIntent().getStringExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentTimeMillis = System.currentTimeMillis();
        initViews(isReplace ? getReplaceResponse() : getBuyResponse());
    }

    private void initViews(int buyResponse) {
        Bundle content = getResponseContent(buyResponse);
        initTextBody(content);
        if (buyResponse == GoogleUtil.RESULT_OK){
            initBodyResultSuccess(content);
        } else {
            initBodyResultFail(buyResponse);
        }
    }

    private void initTextBody(Bundle content) {
        TextView title = (TextView) findViewById(R.id.buy_title);
        title.setText(content.getString(RESPONSE_EXTRA_TITLE));

        TextView summary = (TextView) findViewById(R.id.buy_summary);
        summary.setText(content.getString(RESPONSE_EXTRA_SUMMARY));
    }

    private void initBodyResultSuccess(Bundle content) {
        initUserSpinner();

        // init price
        TextView price = (TextView) findViewById(R.id.buy_price);
        if (content.containsKey(RESPONSE_EXTRA_PRICE)) {
            price.setVisibility(View.VISIBLE);
            price.setText(content.getString(RESPONSE_EXTRA_PRICE));
        } else {
            price.setVisibility(View.GONE);
        }

        findViewById(R.id.div).setVisibility(View.VISIBLE);

        // init buy
        Button buyButton = (Button) findViewById(R.id.buy_button);
        buyButton.setText(R.string.buy);
        buyButton.setOnClickListener(v -> onBuy());
    }

    private void initUserSpinner() {
        Spinner usersSpinner = (Spinner) findViewById(R.id.buy_spinner_accounts);
        usersSpinner.setVisibility(View.VISIBLE);

        String currentUser = apiOverrides.getUsersResponse();
        List<String> users = config.users();
        int index = users.indexOf(currentUser);
        int selectedItem = index == -1 ? 0 : index;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.simple_list_item, android.R.id.text1,
                users);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        usersSpinner.setAdapter(adapter);
        usersSpinner.setSelection(selectedItem, false);
        usersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                apiOverrides.setUsersReponse(users.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });
    }

    private void initBodyResultFail(int buyResponse) {
        findViewById(R.id.buy_price).setVisibility(View.GONE);
        findViewById(R.id.div).setVisibility(View.GONE);
        findViewById(R.id.buy_spinner_accounts).setVisibility(View.GONE);

        Button buyButton = (Button) findViewById(R.id.buy_button);
        if (buyResponse == GoogleUtil.RESULT_ITEM_ALREADY_OWNED) {
            buyButton.setText(R.string.replace);
            buyButton.setOnClickListener(v -> onBuyAlreadyOwned());
        } else {
            buyButton.setText(R.string.ok);
            buyButton.setOnClickListener(v -> finish());
        }
    }

    private void onBuy() {
        String newReceipt = String.format(Locale.getDefault(), RECEIPT_FMT,
                apiOverrides.getUsersResponse(), currentTimeMillis);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_OK);
        String skuToPurchase  = isReplace ? newSku : sku;
        InAppPurchaseData inAppPurchaseData = new InAppPurchaseData.Builder()
                .orderId(Long.toString(currentTimeMillis))
                .packageName(config.skus().get(skuToPurchase).packageName())
                .productId(skuToPurchase)
                .purchaseTime(Long.toString(currentTimeMillis))
                .developerPayload(developerPayload)
                .purchaseToken(newReceipt)
                .build();
        String inAppPurchaseDataStr = gson.toJson(inAppPurchaseData);
        boolean result;
        if (isReplace) {
            result = purchases.replacePurchase(inAppPurchaseDataStr, oldSkus);
        } else {
            result = purchases.addPurchase(inAppPurchaseDataStr, itemtype);
        }
        resultIntent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, inAppPurchaseDataStr);
        setResult(result ? RESULT_OK : RESULT_CANCELED, resultIntent);
        finish();
    }

    private void onBuyAlreadyOwned() {
        Iterator iterator = purchases.getInAppPurchaseData(itemtype).iterator();
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

    private Bundle getResponseContent(int buyResponse) {
        Bundle bundle = new Bundle();
        switch (buyResponse) {
            case GoogleUtil.RESULT_OK:
                ConfigSku configSku = config.skus().get(isReplace ? newSku : sku);

                bundle.putString(RESPONSE_EXTRA_TITLE, configSku.title());
                bundle.putString(RESPONSE_EXTRA_SUMMARY, configSku.description());
                bundle.putString(RESPONSE_EXTRA_PRICE, "$" + configSku.price());

                if (isReplace) {
                    ArrayList<String> oldSkuTitles = new ArrayList<>();
                    for (String oldSku : oldSkus) {
                        oldSkuTitles.add(config.skus().get(oldSku).title());
                    }
                    bundle.putStringArrayList(RESPONSE_EXTRA_REPLACE_OLD_SKU, oldSkuTitles);
                }
                break;
            case GoogleUtil.RESULT_ITEM_UNAVAILABLE:
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error));
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.item_not_found));
                break;
            case GoogleUtil.RESULT_ITEM_ALREADY_OWNED:
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error));
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.item_already_owned));
                break;
            case GoogleUtil.RESULT_ITEM_NOT_OWNED:
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error));
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.replace_item_not_owned));
                break;
            case GoogleUtil.RESULT_DEVELOPER_ERROR:
            case GoogleUtil.RESULT_ERROR:
            default:
                String title = getString(R.string.error);
                bundle.putString(RESPONSE_EXTRA_TITLE, title);
                bundle.putString(RESPONSE_EXTRA_SUMMARY,
                        String.format(Locale.getDefault(), ERROR_FMT, title, buyResponse));
                break;
        }
        return bundle;
    }

    private int getBuyResponse() {
        int response = apiOverrides.getBuyResponse();
        if (response == APIOverrides.RESULT_DEFAULT) {
            if (config.skus().get(sku) == null) {
                response = GoogleUtil.RESULT_ITEM_UNAVAILABLE;
            } else if (purchases.getReceiptsForSkus(ImmutableSet.of(sku), itemtype).size() > 0) {
                response = GoogleUtil.RESULT_ITEM_ALREADY_OWNED;
            } else {
                response = GoogleUtil.RESULT_OK;
            }
        }
        return response;
    }

    private int getReplaceResponse() {
        int response = apiOverrides.getReplaceResponse();
        if (response == APIOverrides.RESULT_DEFAULT) {
            if (config.skus().get(newSku) == null) {
                response = GoogleUtil.RESULT_ITEM_UNAVAILABLE;
            } else if (GoogleUtil.BILLING_TYPE_IAP.equals(itemtype)) {
                response = GoogleUtil.RESULT_ERROR;
            } else if (purchases.getReceiptsForSkus(ImmutableSet.of(newSku), itemtype).size() > 0) {
                response = GoogleUtil.RESULT_ITEM_ALREADY_OWNED;
            } else if (skusNotOwned(oldSkus)) {
                response = GoogleUtil.RESULT_ITEM_NOT_OWNED;
            } else {
                response = GoogleUtil.RESULT_OK;
            }
        }
        return response;
    }

    private boolean skusNotOwned(List<String> skus) {
        return purchases.getReceiptsForSkus(ImmutableSet.copyOf(skus), itemtype).size() < skus.size();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFinishing()) {
            Bundle bundle = new Bundle();
            bundle.putInt(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_USER_CANCELED);
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionHandler.handlePermissionResult(requestCode, this, grantResults);
    }
}
