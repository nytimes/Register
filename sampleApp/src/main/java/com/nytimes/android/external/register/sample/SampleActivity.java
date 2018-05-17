package com.nytimes.android.external.register.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.registerlib.GoogleServiceProvider;
import com.nytimes.android.external.registerlib.GoogleServiceProviderTesting;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SampleActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    static final String TAG = "SampleActivity";
    static final String SKU_IAP = "register.sample.iap";
    static final String SKU_SUB = "register.sample.sub";

    Switch billingApiSwitch;
    ValueAnimator appBarColorAnimator;
    SampleAdapter adapter;
    View emptyView;
    TextView emptyTitle;

    PrefsManager prefsManager;
    GoogleServiceProvider googleServiceProvider;
    CompositeDisposable compositeDisposable;

    PurchasesUpdatedListener purchasesUpdatedListener = (responseCode, purchases) -> checkPurchasesAndSkuDetails();

    BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(int responseCode) {
            if (responseCode == BillingClient.BillingResponse.OK) {
                checkPurchasesAndSkuDetails();
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            //TODO handle disconnection (perhaps reconnection)
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        initFields();
        initToolbar();
        initEmptyView();
        initRecycler();
        initGoogleServiceProvider();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPurchasesAndSkuDetails();
    }

    @Override
    protected void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }

        if (googleServiceProvider != null) {
            googleServiceProvider.endConnection();
        }

        if (adapter != null) {
            adapter.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (billingApiSwitch.equals(buttonView)) {
            prefsManager.setUsingGoogleServiceProvider(isChecked);
            initGoogleServiceProvider();
            checkPurchasesAndSkuDetails();

            if (isChecked) {
                appBarColorAnimator.start();
            } else {
                appBarColorAnimator.reverse();
            }
        }
    }

    void initFields() {
        prefsManager = new PrefsManager(PreferenceManager.getDefaultSharedPreferences(this));
        compositeDisposable = new CompositeDisposable();
    }

    void initToolbar() {
        boolean isUsingTestProvider = prefsManager.isUsingTestGoogleServiceProvider();

        int appBarEnabled = ContextCompat.getColor(this, R.color.colorPrimary);
        int appBarDisabled = ContextCompat.getColor(this, R.color.colorPrimaryAlt);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.setOnClickListener(v -> billingApiSwitch.toggle());
        appBarLayout.setBackgroundColor(isUsingTestProvider ? appBarEnabled : appBarDisabled);
        appBarLayout.setPadding(appBarLayout.getPaddingLeft(),
                getStatusBarHeight(),
                appBarLayout.getPaddingRight(),
                appBarLayout.getPaddingBottom());

        appBarColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), appBarDisabled, appBarEnabled);
        appBarColorAnimator.setCurrentPlayTime(isUsingTestProvider ?
                appBarColorAnimator.getDuration() : 0);
        appBarColorAnimator.addUpdateListener(animator ->
                appBarLayout.setBackgroundColor((int) animator.getAnimatedValue()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        billingApiSwitch = findViewById(R.id.app_bar_switch);
        billingApiSwitch.setChecked(isUsingTestProvider);
        billingApiSwitch.setOnCheckedChangeListener(this);
    }

    void initRecycler() {
        adapter = new SampleAdapter(this);
        compositeDisposable.add(adapter.getClickSubject().subscribe(item -> {
            BillingFlowParams params =
                    BillingFlowParams
                            .newBuilder()
                            .setSku(item.getSku())
                            .setType(item.getType())
                            .build();
            googleServiceProvider.launchBillingFlow(this, params);
        }));

        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        int dividerSize = getResources().getDimensionPixelSize(R.dimen.recycler_divider_size);
        recyclerView.addItemDecoration(new EmptyItemDecoration(dividerSize));
    }

    void initEmptyView() {
        emptyView = findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);

        emptyTitle = findViewById(R.id.empty_message);
        emptyTitle.setText(prefsManager.isUsingTestGoogleServiceProvider() ?
                R.string.empty_message_register : R.string.empty_message_google);
    }

    void initGoogleServiceProvider() {
        googleServiceProvider =
                GoogleServiceProviderTesting
                        .newBuilder(this)
                        .useTestProvider(prefsManager.isUsingTestGoogleServiceProvider())
                        .setListener(purchasesUpdatedListener)
                        .build();

        googleServiceProvider.startConnection(billingClientStateListener);
    }

    void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            emptyTitle.setText(prefsManager.isUsingTestGoogleServiceProvider() ?
                    R.string.empty_message_register : R.string.empty_message_google);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    void checkPurchasesAndSkuDetails() {
        if (googleServiceProvider.isReady()) {
            final SkuDetailsParams iapParams =
                    SkuDetailsParams
                            .newBuilder()
                            .setSkusList(ImmutableList.of(SKU_IAP))
                            .setType(BillingClient.SkuType.INAPP)
                            .build();

            final Observable<List<SkuDetails>> iapSkuDetailsStream =
                    Observable
                            .create(emitter ->
                                    googleServiceProvider
                                            .querySkuDetailsAsync(iapParams, (responseCode, skuDetailsList) -> {
                                                if (skuDetailsList == null) {
                                                    emitter.onNext(new ArrayList<>());
                                                } else {
                                                    emitter.onNext(skuDetailsList);
                                                }
                                                emitter.onComplete();
                                            }));

            final SkuDetailsParams subParams =
                    SkuDetailsParams
                            .newBuilder()
                            .setSkusList(ImmutableList.of(SKU_SUB))
                            .setType(BillingClient.SkuType.SUBS)
                            .build();

            final Observable<List<SkuDetails>> subSkuDetailsStream =
                    Observable
                            .create(emitter ->
                                    googleServiceProvider
                                            .querySkuDetailsAsync(subParams, (responseCode, skuDetailsList) -> {
                                                if (skuDetailsList == null) {
                                                    emitter.onNext(new ArrayList<>());
                                                } else {
                                                    emitter.onNext(skuDetailsList);
                                                }
                                                emitter.onComplete();
                                    }));

            final Observable<List<Purchase>> purchasesResultStream =
                    Observable.
                            fromCallable(() -> {
                                final List<String> skus = ImmutableList.of(SKU_IAP, SKU_SUB);
                                final List<Purchase> purchases = new ArrayList<>();
                                for (String sku : skus) {
                                    Purchase.PurchasesResult result = googleServiceProvider.queryPurchases(sku);
                                    if (result.getResponseCode() == BillingClient.BillingResponse.OK) {
                                        purchases.addAll(result.getPurchasesList());
                                    }
                                }
                                return purchases;
                            });

            compositeDisposable.add(
                    Observable
                            .combineLatest(
                                    iapSkuDetailsStream
                                            .zipWith(
                                                    subSkuDetailsStream,
                                                    (iapSkuDetails, subsSkuDetails) -> {
                                                        final List<SkuDetails> combined = new ArrayList<>();
                                                        combined.addAll(iapSkuDetails);
                                                        combined.addAll(subsSkuDetails);
                                                        return combined;
                                                    }),
                                    purchasesResultStream,
                                    Pair::new
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        adapter.clear();
                                        for (SkuDetails skuDetails : result.first) {
                                            adapter.addItem(skuDetails);
                                        }
                                        for (Purchase purchase : result.second) {
                                            adapter.addPurchase(purchase);
                                        }
                                        checkEmptyState();
                                    },
                                    throwable -> Log.e(TAG, "Failed to retrieve products and purchases", throwable)
                            )
            );
        }
    }

    public int getStatusBarHeight() {
        int result;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } else {
            result = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        }
        return result;
    }
}
