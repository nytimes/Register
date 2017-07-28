package com.nytimes.android.external.register.sample;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.registerlib.GoogleProductResponse;
import com.nytimes.android.external.registerlib.GoogleServiceProvider;
import com.nytimes.android.external.registerlib.GoogleServiceProviderImpl;
import com.nytimes.android.external.registerlib.GoogleServiceProviderTesting;
import com.nytimes.android.external.registerlib.GoogleUtil;
import com.nytimes.android.external.registerlib.InAppPurchaseData;
import com.nytimes.android.external.registerlib.ServiceIntentHelper;

import java.util.LinkedHashSet;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.nytimes.android.external.register.sample.BuyServiceConnection.REQUEST_CODE_GOOGLE_PURCHASE;

public class SampleActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SampleActivity";
    private static final String SKU_IAP = "register.sample.iap";
    private static final String SKU_SUB = "register.sample.sub";
    private static final String DEV_PAYLOAD = "devPayload";

    private Switch billingApiSwitch;
    private ValueAnimator appBarColorAnimator;
    private SampleAdapter adapter;
    private View emptyView;
    private TextView emptyTitle;

    private PrefsManager prefsManager;
    private GoogleServiceProvider googleServiceProvider;
    private Set<ServiceConnection> boundSet;
    private CompositeDisposable compositeDisposable;
    private BuyServiceConnection buyServiceConnection;
    private GetPurchasesAndSkuDetailsConnection getPurchasesConn;

    private final Consumer<GetPurchasesAndSkuDetailsConnection.Response> purchasesAndSkuDetailsConsumer =
            new Consumer<GetPurchasesAndSkuDetailsConnection.Response>() {
                @Override
                public void accept(GetPurchasesAndSkuDetailsConnection.Response response) {
                    unbindConnection(getPurchasesConn);
                    adapter.clear();
                    handlePurchasesBundles(response.iapPurchases(), response.subPurchases());
                    handleSkuDetailsBundles(response.iapSkuDetails(), response.subSkuDetails());
                    checkEmptyState();
                }
            };

    private final Consumer<Throwable> purchasesAndSkuDetailsError = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            unbindConnection(getPurchasesConn);
            Log.e(TAG, "getPurchasesAndSkuDetails error", throwable);
        }
    };

    private final Consumer<PendingIntent> buyPendingIntentConsumer = new Consumer<PendingIntent>() {
        @Override
        public void accept(PendingIntent pendingIntent) throws IntentSender.SendIntentException {
            unbindConnection(buyServiceConnection);
            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    REQUEST_CODE_GOOGLE_PURCHASE, new Intent(), 0, 0, 0);
        }
    };

    private final Consumer<Throwable> buyPendingIntentError = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            unbindConnection(buyServiceConnection);
            Log.e(TAG, "getBuyIntent error", throwable);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_PURCHASE && data != null) {
            int responseCode = data.getIntExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_USER_CANCELED);
            if (responseCode == GoogleUtil.RESULT_OK) {
                checkPurchasesAndSkuDetails();
            }
        }
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

            if (isChecked){
                appBarColorAnimator.start();
            } else {
                appBarColorAnimator.reverse();
            }
        }
    }

    private void initFields() {
        prefsManager = new PrefsManager(PreferenceManager.getDefaultSharedPreferences(this));
        compositeDisposable = new CompositeDisposable();
        boundSet = new LinkedHashSet<>();
    }

    private void initToolbar() {
        boolean isUsingTestProvider = prefsManager.isUsingTestGoogleServiceProvider();

        int appBarEnabled = ContextCompat.getColor(this, R.color.colorPrimary);
        int appBarDisabled = ContextCompat.getColor(this, R.color.colorPrimaryAlt);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        billingApiSwitch = (Switch) findViewById(R.id.app_bar_switch);
        billingApiSwitch.setChecked(isUsingTestProvider);
        billingApiSwitch.setOnCheckedChangeListener(this);
    }

    private void initRecycler() {
        adapter = new SampleAdapter(this);
        compositeDisposable.add(adapter.getClickSubject().subscribe(item -> {
            Intent intent = ServiceIntentHelper.createExplicitFromImplicitIntent(
                    this, googleServiceProvider.getIntent());
            if (intent != null) {
                buyServiceConnection = new BuyServiceConnection(
                        item.productId(),
                        item.itemType(),
                        getPackageName(),
                        DEV_PAYLOAD,
                        googleServiceProvider);
                compositeDisposable.add(buyServiceConnection.getBuyPendingIntent()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(buyPendingIntentConsumer, buyPendingIntentError));
                bindService(intent, buyServiceConnection, Context.BIND_AUTO_CREATE);
                boundSet.add(buyServiceConnection);
            }
        }));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        int dividerSize = getResources().getDimensionPixelSize(R.dimen.recycler_divider_size);
        recyclerView.addItemDecoration(new EmptyItemDecoration(dividerSize));
    }

    private void initEmptyView(){
        emptyView = findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);

        emptyTitle = (TextView) findViewById(R.id.empty_message);
        emptyTitle.setText(prefsManager.isUsingTestGoogleServiceProvider() ?
                R.string.empty_message_register : R.string.empty_message_google);
    }

    private void initGoogleServiceProvider() {
        if (prefsManager.isUsingTestGoogleServiceProvider()) {
            googleServiceProvider = new GoogleServiceProviderTesting();
        } else {
            googleServiceProvider = new GoogleServiceProviderImpl();
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0){
            emptyView.setVisibility(View.VISIBLE);
            emptyTitle.setText(prefsManager.isUsingTestGoogleServiceProvider() ?
                    R.string.empty_message_register : R.string.empty_message_google);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void checkPurchasesAndSkuDetails() {
        Intent intent = ServiceIntentHelper.createExplicitFromImplicitIntent(this, googleServiceProvider.getIntent());
        if (intent != null) {
            getPurchasesConn = new GetPurchasesAndSkuDetailsConnection(ImmutableList.of(SKU_IAP),
                    ImmutableList.of(SKU_SUB), getPackageName(), googleServiceProvider);
            compositeDisposable.add(getPurchasesConn.getPurchasesAndSkuDetails()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(purchasesAndSkuDetailsConsumer, purchasesAndSkuDetailsError));
            bindService(intent, getPurchasesConn, Context.BIND_AUTO_CREATE);
            boundSet.add(getPurchasesConn);
        }
    }

    private void handlePurchasesBundles(Bundle iapBundle, Bundle subBundle) {
        int iapResponse = iapBundle.getInt(GoogleUtil.RESPONSE_CODE);
        int subResponse = subBundle.getInt(GoogleUtil.RESPONSE_CODE);
        if (iapResponse == GoogleUtil.RESULT_OK && subResponse == GoogleUtil.RESULT_OK) {
            for (String json : ImmutableList.<String>builder()
                    .addAll(iapBundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                    .addAll(subBundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                    .build()) {
                InAppPurchaseData inAppPurchaseData = InAppPurchaseData.fromJson(json);
                adapter.addPurchase(inAppPurchaseData);
            }
        } else {
            Log.e(TAG, "getPurchases returned iap: " + iapResponse + "; sub: " + subResponse);
        }
    }

    private void handleSkuDetailsBundles(Bundle iapBundle, Bundle subBundle) {
        int iapResponse = iapBundle.getInt(GoogleUtil.RESPONSE_CODE);
        int subResponse = subBundle.getInt(GoogleUtil.RESPONSE_CODE);
        if (iapResponse == GoogleUtil.RESULT_OK && subResponse == GoogleUtil.RESULT_OK) {
            for (String json : ImmutableList.<String>builder()
                    .addAll(iapBundle.getStringArrayList(GoogleUtil.DETAILS_LIST))
                    .addAll(subBundle.getStringArrayList(GoogleUtil.DETAILS_LIST))
                    .build()) {
                GoogleProductResponse googleProductResponse = GoogleProductResponse.fromJson(json);
                adapter.addItem(googleProductResponse);
            }
        } else {
            Log.e(TAG, "getSkuDetails returned iap: " + iapResponse + "; sub: " + subResponse);
        }
    }

    void unbindConnection(ServiceConnection conn) {
        if (conn != null && boundSet.contains(conn)) {
            unbindService(conn);
            boundSet.remove(conn);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        } else {
            result = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        }
        return result;
    }
}
