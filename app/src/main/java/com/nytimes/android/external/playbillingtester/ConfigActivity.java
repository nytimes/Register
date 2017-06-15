package com.nytimes.android.external.playbillingtester;


import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nytimes.android.external.playbillingtester.di.Injector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Override default return values from API calls
 * * Display/Purge purchased items
 */
@SuppressWarnings("PMD.UseVarargs")
public class ConfigActivity extends AppCompatActivity {

    @Inject
    protected APIOverrides apiOverrides;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        initScrim();
        initToolbar();
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initScrim() {
        findViewById(R.id.scrim).setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.register);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        initSpinner(R.id.isBillingSupported, R.string.is_billing_supported, R.array.isBillingEnabled_spinner);
        initSpinner(R.id.getBuyIntent, R.string.get_buy_intent, R.array.getBuyIntent_spinner);
        initSpinner(R.id.buy, R.string.buy_label, R.array.buy_spinner);
        initSpinner(R.id.getPurchases, R.string.get_purchases, R.array.getPurchases_spinner);
        initSpinner(R.id.getSkuDetails, R.string.get_sku_details, R.array.getSkuDetails_spinner);
        initSpinner(R.id.consumePurchase, R.string.consume_purchases, R.array.consumePurchase_spinner);
        initSpinner(R.id.getBuyIntentToReplaceSkus, R.string.buy_intent_replace_skus, R.array.debug_spinner);
        initSpinner(R.id.replace, R.string.replace, R.array.debug_spinner);
    }

    private void initSpinner(@IdRes int containerLayoutId, @StringRes int titleResId, @ArrayRes int entriesResId) {
        View container = findViewById(containerLayoutId);

        // Set title
        TextView title = container.findViewById(R.id.config_title);
        title.setText(titleResId);

        // Get data
        List<ConfigResponse> items = getData(entriesResId);

        // Init Spinner
        Spinner spinner = container.findViewById(R.id.config_spinner);
        spinner.setTag("IGNORE_FIRST_SELECT");
        ConfigSpinnerAdapter configResponseAdapter =
                new ConfigSpinnerAdapter(this, items);
        spinner.setAdapter(configResponseAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getTag() == null) {
                    ConfigResponse item = (ConfigResponse) configResponseAdapter.getItem(position);
                    setApiOverridesValue(containerLayoutId, item);
                } else {
                    spinner.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });

        int currentConfigResponseCode = getApiOverridesValue(containerLayoutId);
        int spinnerSelectionIndex = getResponseCodeListIndex(currentConfigResponseCode, items);
        if (spinnerSelectionIndex != -1) {
            spinner.setSelection(spinnerSelectionIndex, false);
        }
    }

    @NonNull
    private List<ConfigResponse> getData(@ArrayRes int entriesResId) {
        List<ConfigResponse> items = new ArrayList<>();
        String[] entries = getResources().getStringArray(entriesResId);
        for (String id : entries) {
            items.add(ImmutableConfigResponse.builder()
                    .responseId(id)
                    .responseCode(getConfigResponseCode(id))
                    .responseName(getConfigResponseName(id))
                    .build());
        }
        return items;
    }

    private String getConfigResponseName(String name) {
        int resId = getResources().getIdentifier(name, "string", getPackageName());
        return getResources().getString(resId);
    }

    private int getConfigResponseCode(String name) {
        String resourceCodeName = name + "_code";
        int resId = getResources().getIdentifier(resourceCodeName, "integer", getPackageName());
        return getResources().getInteger(resId);
    }

    private void setApiOverridesValue(int containerLayoutId, ConfigResponse item) {
        switch (containerLayoutId) {
            case R.id.isBillingSupported:
                apiOverrides.setIsBillingSupportedResponse(item.responseCode());
                break;
            case R.id.getBuyIntent:
                apiOverrides.setGetBuyIntentResponse(item.responseCode());
                break;
            case R.id.buy:
                apiOverrides.setBuyResponse(item.responseCode());
                break;
            case R.id.getPurchases:
                apiOverrides.setGetPurchasesResponse(item.responseCode());
                break;
            case R.id.getSkuDetails:
                apiOverrides.setGetSkuDetailsResponse(item.responseCode());
                break;
            case R.id.consumePurchase:
                apiOverrides.setConsumePurchaseResponse(item.responseCode());
                break;
            case R.id.getBuyIntentToReplaceSkus:
            case R.id.replace:
            default:
                // unknown id
                break;
        }
    }

    private int getApiOverridesValue(@IdRes int containerLayoutId) {
        switch (containerLayoutId) {
            case R.id.isBillingSupported:
                return apiOverrides.getIsBillingSupportedResponse();
            case R.id.getBuyIntent:
                return apiOverrides.getGetBuyIntentResponse();
            case R.id.buy:
                return apiOverrides.getBuyResponse();
            case R.id.getPurchases:
                return apiOverrides.getGetPurchasesResponse();
            case R.id.getSkuDetails:
                return apiOverrides.getGetSkuDetailsResponse();
            case R.id.consumePurchase:
                return apiOverrides.getConsumePurchaseResponse();
            case R.id.getBuyIntentToReplaceSkus:
            case R.id.replace:
            default:
                return -1;
        }
    }

    private int getResponseCodeListIndex(int responseCode, List<ConfigResponse> items) {
        int index = 0;
        for (int i = 0, size = items.size(); i < size; i++) {
            ConfigResponse item = items.get(i);
            if (item.responseCode() == responseCode) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.fade_out);
    }

}
