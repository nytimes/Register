package com.nytimes.android.external.register;

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
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.nytimes.android.external.register.di.Injector;
import com.nytimes.android.external.register.model.Config;
import com.nytimes.android.external.register.products.ProductsActivity;
import com.nytimes.android.external.registerlib.GoogleUtil;
import com.nytimes.android.external.registerlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Display/Purge purchased items
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    protected Purchases purchases;
    @Inject
    protected APIOverridesDelegate apiDelegate;
    @Inject
    protected Optional<Config> config;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private MainAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private AppBarLayout appBarLayout;
    private MenuItem configureMenuItem;
    private TextView emptyViewTitle;
    private TextView emptyViewText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initAppBarLayout();
        initRecycler();

        emptyView = findViewById(R.id.empty_view);
        emptyViewText = (TextView) findViewById(R.id.empty_view_text);
        emptyViewTitle = (TextView) findViewById(R.id.empty_view_title);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
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
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);

        adapter = new MainAdapter(this);
        adapter.setHasStableIds(true);
        disposables.add(adapter.getDeleteSubject().subscribe(pair -> {
            String inAppPurchaseDataStr = InAppPurchaseData.toJson(pair.second);
            if (purchases.removePurchase(inAppPurchaseDataStr, pair.first)) {
                adapter.remove(pair);
                checkEmptyState();
            }
        }));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
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
        initSpinner(R.id.getBuyIntentToReplaceSkus, R.string.buy_intent_replace_skus,
                R.array.buy_intent_replace_skus_spinner);
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
                    apiDelegate.setApiOverridesValue(containerLayoutId, item);
                } else {
                    spinner.setTag(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });

        int currentConfigResponseCode = apiDelegate.getApiOverridesValue(containerLayoutId);
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
        if (config.isPresent()) {
            List<Pair<String, InAppPurchaseData>> items = new ArrayList<>();
            for (InAppPurchaseData sub : purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION)) {
                items.add(Pair.create(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, sub));
            }
            for (InAppPurchaseData iap : purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP)) {
                items.add(Pair.create(GoogleUtil.BILLING_TYPE_IAP, iap));
            }
            Collections.sort(items, (l, r) -> {
                long purchaseTimeL = Long.parseLong(l.second.purchaseTime());
                long purchaseTimeR = Long.parseLong(r.second.purchaseTime());
                return Long.compare(purchaseTimeR, purchaseTimeL);
            });
            adapter.setItems(items);
        }
    }

    private void checkEmptyState() {
        emptyView.setVisibility(!config.isPresent() || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        if (emptyView.getVisibility() == View.VISIBLE) {
            emptyViewText.setText(config.isPresent() ? R.string.empty_message_text : R.string.no_config_text);
            emptyViewTitle.setText(config.isPresent() ? R.string.empty_message_title : R.string.no_config_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        configureMenuItem = menu.findItem(R.id.menu_action_configure);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_delete_all:
                purchases.purgePurchases();
                swipeRefresh.setRefreshing(true);
                swipeRefresh.postDelayed(this::onRefresh, 300);
                return true;
            case R.id.menu_action_refresh:
                swipeRefresh.setRefreshing(true);
                swipeRefresh.postDelayed(this::onRefresh, 300);
                return true;
            case R.id.menu_action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_action_configure:
                appBarLayout.setExpanded(true, true);
                return true;
            case R.id.menu_action_products:
                Intent productsIntent = new Intent(this, ProductsActivity.class);
                startActivity(productsIntent);
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

    @Override
    public void onRefresh() {
        swipeRefresh.postDelayed(() -> swipeRefresh.setRefreshing(false), 300);
        updatePurchases();
        checkEmptyState();
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}
