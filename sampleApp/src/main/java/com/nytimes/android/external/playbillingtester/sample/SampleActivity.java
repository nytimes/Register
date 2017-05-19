package com.nytimes.android.external.playbillingtester.sample;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtesterlib.GoogleProductResponse;
import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProvider;
import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProviderImpl;
import com.nytimes.android.external.playbillingtesterlib.GoogleServiceProviderTesting;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;
import com.nytimes.android.external.playbillingtesterlib.ServiceIntentHelper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.nytimes.android.external.playbillingtester.sample.BuyServiceConnection.REQUEST_CODE_GOOGLE_PURCHASE;

public class SampleActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {

    private static final String TAG = "SampleActivity";
    private static final String SKU_IAP = "playbillingtester.sample.iap";
    private static final String SKU_SUB = "playbillingtester.sample.sub";

    @BindView(R.id.testerSwitch)
    Switch testerSwitch;

    @BindView(R.id.buySubButton)
    Button buySubButton;

    @BindView(R.id.buyIAPButton)
    Button buyIAPButton;

    Unbinder unbinder;
    PrefsManager prefsManager;
    GoogleServiceProvider googleServiceProvider;
    Gson gson;
    Map<String, InAppPurchaseData> purchaseDataMap;
    Map<String, GoogleProductResponse> skuDetailMap;
    Set<ServiceConnection> boundSet;
    CompositeDisposable compositeDisposable;
    BuyServiceConnection buyServiceConnection;
    GetPurchasesAndSkuDetailsConnection getPurchasesConn;

    private final Consumer<GetPurchasesAndSkuDetailsConnection.Response> purchasesAndSkuDetailsConsumer =
            new Consumer<GetPurchasesAndSkuDetailsConnection.Response>() {
        @Override
        public void accept(GetPurchasesAndSkuDetailsConnection.Response response) {
            unbindConnection(getPurchasesConn);
            purchaseDataMap.clear();
            skuDetailMap.clear();
            handlePurchasesBundles(response.iapPurchases(), response.subPurchases());
            handleSkuDetailsBundles(response.iapSkuDetails(), response.subSkuDetails());
            initWidgets();
        }
    };

    private final Consumer<Throwable> purchasesAndSkuDetailsError = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            unbindConnection(getPurchasesConn);
            Log.e(TAG, "getPurchasesAndSkuDetails error", throwable);
        }
    };

    private void handlePurchasesBundles(Bundle iapBundle, Bundle subBundle) {
        int iapResponse = iapBundle.getInt(GoogleUtil.RESPONSE_CODE);
        int subResponse = subBundle.getInt(GoogleUtil.RESPONSE_CODE);
        if (iapResponse == GoogleUtil.RESULT_OK && subResponse == GoogleUtil.RESULT_OK) {
            for (String json : ImmutableList.<String>builder()
                    .addAll(iapBundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                    .addAll(subBundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                    .build()) {
                InAppPurchaseData inAppPurchaseData = gson.fromJson(json, InAppPurchaseData.class);
                purchaseDataMap.put(inAppPurchaseData.productId(), inAppPurchaseData);
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
                GoogleProductResponse googleProductResponse = gson.fromJson(json, GoogleProductResponse.class);
                skuDetailMap.put(googleProductResponse.productId(), googleProductResponse);
            }
        } else {
            Log.e(TAG, "getSkuDetails returned iap: " + iapResponse + "; sub: " + subResponse);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefsManager = new PrefsManager(PreferenceManager.getDefaultSharedPreferences(this));
        gson = new Gson();
        compositeDisposable = new CompositeDisposable();
        purchaseDataMap = new LinkedHashMap<>();
        skuDetailMap = new LinkedHashMap<>();
        boundSet = new LinkedHashSet<>();
        initGoogleServiceProvider();
        checkPurchasesAndSkuDetails();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        super.onDestroy();
    }

    private void initWidgets() {
        unbinder = ButterKnife.bind(this);
        testerSwitch.setOnCheckedChangeListener(null);
        testerSwitch.setChecked(prefsManager.isUsingTestGoogleServiceProvider());
        testerSwitch.setOnCheckedChangeListener(this);
        initButton(buyIAPButton, SKU_IAP);
        initButton(buySubButton, SKU_SUB);
    }

    private void initButton(Button button, String sku) {
        boolean isPurchased = purchaseDataMap.containsKey(sku);
        button.setEnabled(!isPurchased);
        GoogleProductResponse skuDetail = skuDetailMap.get(sku);
        button.setText(String.format(getString(isPurchased ? R.string.own_label : R.string.buy_label),
                skuDetail == null ? sku : skuDetail.title()));
    }

    private void initGoogleServiceProvider() {
        if (prefsManager.isUsingTestGoogleServiceProvider()) {
            googleServiceProvider = new GoogleServiceProviderTesting();
        } else {
            googleServiceProvider = new GoogleServiceProviderImpl();
        }
    }

    @OnCheckedChanged({R.id.testerSwitch})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (testerSwitch.equals(buttonView)) {
            prefsManager.setUsingGoogleServiceProvider(isChecked);
            initGoogleServiceProvider();
            checkPurchasesAndSkuDetails();
        }
    }

    @OnClick({R.id.buyIAPButton, R.id.buySubButton})
    public void onClick(View view) {
        if (buyIAPButton.equals(view)) {
            buy(SKU_IAP, GoogleUtil.BILLING_TYPE_IAP);
        } else if (buySubButton.equals(view)) {
            buy(SKU_SUB, GoogleUtil.BILLING_TYPE_SUBSCRIPTION);
        }
    }

    private void buy(String sku, String type) {
        Intent intent = ServiceIntentHelper.createExplicitFromImplicitIntent(this, googleServiceProvider.getIntent());
        if (intent != null) {
            buyServiceConnection = new BuyServiceConnection(sku, type, this, googleServiceProvider);
            bindService(intent, buyServiceConnection, Context.BIND_AUTO_CREATE);
            boundSet.add(buyServiceConnection);
        }
    }

    private void checkPurchasesAndSkuDetails() {
        Intent intent = ServiceIntentHelper.createExplicitFromImplicitIntent(this, googleServiceProvider.getIntent());
        if (intent != null) {
            getPurchasesConn = new GetPurchasesAndSkuDetailsConnection(ImmutableList.of(SKU_IAP),
                    ImmutableList.of(SKU_SUB), this, googleServiceProvider);
            compositeDisposable.add(getPurchasesConn.getPurchasesAndSkuDetails()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(purchasesAndSkuDetailsConsumer, purchasesAndSkuDetailsError));
            bindService(intent, getPurchasesConn, Context.BIND_AUTO_CREATE);
            boundSet.add(getPurchasesConn);
        }
    }

    void unbindConnection(ServiceConnection conn) {
        if (conn != null && boundSet.contains(conn)) {
            unbindService(conn);
            boundSet.remove(conn);
        }
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
}
