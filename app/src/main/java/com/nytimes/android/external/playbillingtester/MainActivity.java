package com.nytimes.android.external.playbillingtester;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.nytimes.android.external.playbillingtester.di.Injector;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Override default return values from API calls
 * * Display/Purge purchased items
 */
@SuppressWarnings("PMD.UseVarargs")
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final String VERSION_FMT = "Version %s(%d)";

    @Inject
    protected APIOverridesAndPurchases apiOverridesAndPurchases;
    @Inject
    protected Config config;
    @Inject
    protected AlertDialog.Builder dialogBuilder;
    @BindView(R.id.isBillingSupported)
    Spinner isBillingSupportedSpinner;
    @BindView(R.id.getBuyIntent)
    Spinner getBuyIntentSpinner;
    @BindView(R.id.buy)
    Spinner buySpinner;
    @BindView(R.id.getPurchases)
    Spinner getPurchasesSpinner;
    @BindView(R.id.consumePurchase)
    Spinner consumePurchaseSpinner;
    @BindView(R.id.getSkuDetails)
    Spinner getSkuDetailsSpinner;
    @BindView(R.id.usersSpinner)
    Spinner usersSpinner;
    @BindView(R.id.purgeButton)
    Button purgeButton;
    @BindView(R.id.refreshButton)
    Button refreshButton;
    @BindView(R.id.items)
    TextView itemsTextView;
    Unbinder unbinder;
    SparseBooleanArray checkedMap = new SparseBooleanArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        setContentView(R.layout.activity_play_billing_tester);
        unbinder = ButterKnife.bind(this);
        updateSpinners();
        updateItemsTextView();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroy();
    }

    void updateSpinners() {
        ArrayAdapter<String> usersSpinnerArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, config.users());
        usersSpinner.setAdapter(usersSpinnerArrayAdapter);

        // set spinners from model
        isBillingSupportedSpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.isBillingEnabled_spinner,
                apiOverridesAndPurchases.getIsBillingSupportedResponse()));
        getBuyIntentSpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.getBuyIntent_spinner,
                apiOverridesAndPurchases.getGetBuyIntentResponse()));
        buySpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.buy_spinner,
                apiOverridesAndPurchases.getBuyResponse()));
        getPurchasesSpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.getPurchases_spinner,
                apiOverridesAndPurchases.getGetPurchasesResponse()));
        getSkuDetailsSpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.getSkuDetails_spinner,
                apiOverridesAndPurchases.getGetSkuDetailsResponse()));
        consumePurchaseSpinner.setSelection(getSpinnerArrayPositionFromCode(R.array.consumePurchase_spinner,
                apiOverridesAndPurchases.getConsumePurchaseResponse()));
        usersSpinner.setSelection(getSpinnerArrayPosition(config.users(),
                apiOverridesAndPurchases.getUsersResponse()));
    }
    void updateItemsTextView() {
        StringBuffer buf = new StringBuffer();
        for (InAppPurchaseData inAppPurchaseData :
                apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION)) {
            appendInAppPurchaseLine(buf, inAppPurchaseData);
        }
        for (InAppPurchaseData inAppPurchaseData :
                apiOverridesAndPurchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP)) {
            appendInAppPurchaseLine(buf, inAppPurchaseData);
        }
        itemsTextView.setText(buf);
    }

    private StringBuffer appendInAppPurchaseLine(StringBuffer buf, InAppPurchaseData inAppPurchaseData) {
        return buf.append(inAppPurchaseData.productId()).append(';').append(inAppPurchaseData.purchaseToken())
                .append('\n');

    }

    private int getSpinnerArrayPositionFromCode(int stringArrayId, int value) {
        String val[] = getResources().getStringArray(stringArrayId);
        int index = 0;
        for (String s : val) {
            if (s.startsWith(Integer.toString(value) + " ")) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private int getSpinnerArrayPosition(List list, String selectedItem) {
        int i = 0;
        for (Object item : list) {
            if (item.equals(selectedItem)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.play_in_app_billing_tester, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            dialogBuilder
                    .setTitle(R.string.app_name)
                    .setMessage(String.format(Locale.getDefault(), VERSION_FMT, BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE))
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionHandler.handlePermissionResult(requestCode, this, grantResults);
    }

    @OnClick(R.id.purgeButton)
    public void handlePurge(View v) {
        apiOverridesAndPurchases.purgePurchases();
        updateItemsTextView();
    }

    @OnClick(R.id.refreshButton)
    public void handleRefresh(View v) {
        updateItemsTextView();
    }

    @Override
    @OnItemSelected({R.id.isBillingSupported, R.id.getBuyIntent, R.id.buy, R.id.getPurchases, R.id.getSkuDetails,
            R.id.usersSpinner})
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (checkedMap.get(parent.getId())) { // we want to ignore 1st call onCreate
            switch (parent.getId()) {
                case R.id.isBillingSupported:
                    apiOverridesAndPurchases.setIsBillingSupportedResponse(
                            getCodeFromSpinnerItem(R.array.isBillingEnabled_spinner, position));
                    break;
                case R.id.getBuyIntent:
                    apiOverridesAndPurchases.setGetBuyIntentResponse(
                            getCodeFromSpinnerItem(R.array.getBuyIntent_spinner, position));
                    break;
                case R.id.buy:
                    apiOverridesAndPurchases.setBuyResponse(
                            getCodeFromSpinnerItem(R.array.buy_spinner, position));
                    break;
                case R.id.getPurchases:
                    apiOverridesAndPurchases.setGetPurchasesResponse(
                            getCodeFromSpinnerItem(R.array.getPurchases_spinner, position));
                    break;
                case R.id.getSkuDetails:
                    apiOverridesAndPurchases.setGetSkuDetailsResponse(
                            getCodeFromSpinnerItem(R.array.getSkuDetails_spinner, position));
                    break;
                case R.id.consumePurchase:
                    apiOverridesAndPurchases.setConsumePurchaseResponse(
                            getCodeFromSpinnerItem(R.array.consumePurchase_spinner, position));
                case R.id.usersSpinner:
                    apiOverridesAndPurchases.setUsersReponse(config.users().get(position));
                    break;
                default:
                    // unknown id
                    break;
            }
        } else {
            checkedMap.put(parent.getId(), true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // intentionally empty
    }

    int getCodeFromSpinnerItem(int arrayId, int position) {
        return Integer.decode(getResources().getStringArray(arrayId)[position].split(" ")[0]);
    }
}
