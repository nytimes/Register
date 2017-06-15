package com.nytimes.android.external.playbillingtester;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
 * * Override default return values from API calls
 * * Display/Purge purchased items
 */
@SuppressWarnings("PMD.UseVarargs")
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    @Inject
    protected Purchases purchases;

    private MainAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initRecycler();
        initSwipeRefresh();

        emptyView = findViewById(R.id.empty_view);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.register);
        setSupportActionBar(toolbar);
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

        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
    }

    private void initSwipeRefresh() {
        swipeRefresh = findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
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
                Intent configureIntent = new Intent(this, ConfigActivity.class);
                startActivity(configureIntent);
                overridePendingTransition(R.anim.fade_in, 0);
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
