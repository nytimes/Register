package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nytimes.android.external.playbillingtester.di.Injector;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Display/Purge purchased items
 */
@SuppressWarnings("PMD.GodClass")
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    @Inject
    protected Purchases purchases;
    @Inject
    protected APIOverrides apiOverrides;

    private MainAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private AppBarLayout appBarLayout;
    private MenuItem configureMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initAppBarLayout();
        initRecycler();
        initSwipeRefresh();

        emptyView = findViewById(R.id.empty_view);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.register);
        setSupportActionBar(toolbar);
    }

    private void initAppBarLayout() {
        View appBarContent = findViewById(R.id.app_bar_content);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        appBarLayout.setExpanded(false, false);
        appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) -> {
            appBarContent.setTranslationY(-verticalOffset);

            float verticalOffsetAbs = Math.abs(verticalOffset);
            float totalScrollRange = appBarLayout.getTotalScrollRange();
            float normalizedOffset = verticalOffsetAbs / totalScrollRange;

            if (configureMenuItem != null) {
                configureMenuItem.getIcon().setAlpha((int) (normalizedOffset * 255));
                configureMenuItem.setVisible(normalizedOffset > 0);
            }

            float inverseVerticalOffset = totalScrollRange + verticalOffset;
            float startCenter = emptyView.getHeight() / 2f;
            float endCenter = (emptyView.getHeight() - inverseVerticalOffset) / 2;
            float emptyYOffset = startCenter - endCenter;
            emptyView.setTranslationY(-emptyYOffset);
        });
    }

    private void initRecycler() {
        adapter = new MainAdapter(this);
        adapter.setHasStableIds(true);
        adapter.setCallback(new MainAdapter.OnItemCallback() {
            @Override
            public void onItemClicked(InAppPurchaseData item) {
                // No op
            }

            @Override
            public void onItemDeleted(InAppPurchaseData item) {
                Toast.makeText(MainActivity.this, "Delete " + item, Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
    }

    private void initSwipeRefresh() {
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
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
        initSpinner(R.id.replace, R.string.config_replace, R.array.debug_spinner);
    }

    private void initSpinner(@IdRes int containerLayoutId, @StringRes int titleResId, @ArrayRes int entriesResId) {
        View container = findViewById(containerLayoutId);

        // Set title
        TextView title = (TextView) container.findViewById(R.id.config_title);
        title.setText(titleResId);

        // Get data
        List<ConfigResponse> items = getData(entriesResId);

        // Init Spinner
        Spinner spinner = (Spinner) container.findViewById(R.id.config_spinner);
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

    @NonNull
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
    protected void onPostResume() {
        super.onPostResume();
        updatePurchases();
        checkEmptyState();
    }

    private void updatePurchases() {
        List<InAppPurchaseData> items = new ArrayList<>();
        items.addAll(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION));
        items.addAll(purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP));
        Collections.sort(items, (l, r) -> {
            long purchaseTimeL = Long.parseLong(l.purchaseTime());
            long purchaseTimeR = Long.parseLong(r.purchaseTime());
            return Long.compare(purchaseTimeR, purchaseTimeL);
        });
        adapter.setItems(items);
    }

    private void checkEmptyState() {
        emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        adapter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        configureMenuItem = menu.findItem(R.id.menu_action_configure);
        return true;
    }

    @Override
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_delete_all:
                purchases.purgePurchases();
                startRefresh();
                return true;
            case R.id.menu_action_refresh:
                startRefresh();
                return true;
            case R.id.menu_action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_action_configure:
                appBarLayout.setExpanded(true, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionHandler.handlePermissionResult(requestCode, this, grantResults);
    }

    private void startRefresh() {
        swipeRefresh.setRefreshing(true);
        swipeRefresh.postDelayed(this::onRefresh, 300);
    }

    @Override
    public void onRefresh() {
        swipeRefresh.postDelayed(() -> swipeRefresh.setRefreshing(false), 300);
        updatePurchases();
        checkEmptyState();
    }

}
