package com.nytimes.android.external.playbillingtester;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static com.nytimes.android.external.playbillingtester.APIOverrides.RESULT_DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(constants = BuildConfig.class, sdk = 21)
@SuppressWarnings("PMD.MethodNamingConventions")
public class ConfigActivityTest {

    @Mock
    private APIOverrides mockApiOverrides;

    private ConfigActivity testObject;
    private ActivityController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(ConfigActivityTest.TestConfigActivity.class).create();
        testObject = (ConfigActivity) controller.get();
        testObject.apiOverrides = mockApiOverrides;
    }

    @Test
    public void onCreate_whenDefaultValues_showsDefaultValues() {
        when(mockApiOverrides.getIsBillingSupportedResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetBuyIntentResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getBuyResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetPurchasesResponse()).thenReturn(RESULT_DEFAULT);
        when(mockApiOverrides.getGetSkuDetailsResponse()).thenReturn(RESULT_DEFAULT);

        controller.postCreate(null).start();

        assertThat(getSpinnerValue(R.id.isBillingSupported)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getBuyIntent)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.buy)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getPurchases)).isEqualTo(RESULT_DEFAULT);
        assertThat(getSpinnerValue(R.id.getSkuDetails)).isEqualTo(RESULT_DEFAULT);
    }

    @Test
    public void onCreate_whenUniqueValues_showsUniqueValues() {
        when(mockApiOverrides.getIsBillingSupportedResponse())
                .thenReturn(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        when(mockApiOverrides.getGetBuyIntentResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(mockApiOverrides.getBuyResponse()).thenReturn(GoogleUtil.RESULT_ITEM_UNAVAILABLE);
        when(mockApiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_DEVELOPER_ERROR);
        when(mockApiOverrides.getGetSkuDetailsResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

        controller.postCreate(null).start();

        assertThat(getSpinnerValue(R.id.isBillingSupported)).isEqualTo(GoogleUtil.RESULT_BILLING_UNAVAILABLE);
        assertThat(getSpinnerValue(R.id.getBuyIntent)).isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(getSpinnerValue(R.id.buy)).isEqualTo(GoogleUtil.RESULT_ITEM_UNAVAILABLE);
        assertThat(getSpinnerValue(R.id.getPurchases)).isEqualTo(GoogleUtil.RESULT_DEVELOPER_ERROR);
        assertThat(getSpinnerValue(R.id.getSkuDetails)).isEqualTo(GoogleUtil.RESULT_ERROR);
    }


    @Test
    public void onItemSelected_whenChanged_setsApiOverrides() {
        controller.postCreate(null).start();

        AdapterView<Adapter> isBillingSupportedSpinner = getInternalSpinner(R.id.isBillingSupported);
        AdapterView<Adapter> getBuyIntentSpinner = getInternalSpinner(R.id.getBuyIntent);
        AdapterView<Adapter> buySpinner = getInternalSpinner(R.id.buy);
        AdapterView<Adapter> getPurchasesSpinner = getInternalSpinner(R.id.getPurchases);
        AdapterView<Adapter> getSkuDetailsSpinner = getInternalSpinner(R.id.getSkuDetails);

        assertThat(isBillingSupportedSpinner.getTag()).isNull();
        assertThat(getBuyIntentSpinner.getTag()).isNull();
        assertThat(buySpinner.getTag()).isNull();
        assertThat(getPurchasesSpinner.getTag()).isNull();
        assertThat(getSkuDetailsSpinner.getTag()).isNull();

        AdapterView parentView = mock(AdapterView.class);
        View view = mock(View.class);

        isBillingSupportedSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 0, 0L);
        verify(mockApiOverrides).setIsBillingSupportedResponse(RESULT_DEFAULT);

        getBuyIntentSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 1, 0L);
        verify(mockApiOverrides).setGetBuyIntentResponse(GoogleUtil.RESULT_OK);

        buySpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 2, 0L);
        verify(mockApiOverrides).setBuyResponse(GoogleUtil.RESULT_ITEM_UNAVAILABLE);

        getPurchasesSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 3, 0L);
        verify(mockApiOverrides).setGetPurchasesResponse(GoogleUtil.RESULT_ERROR);

        getSkuDetailsSpinner.getOnItemSelectedListener().onItemSelected(parentView, view, 4, 0L);
        verify(mockApiOverrides).setGetSkuDetailsResponse(GoogleUtil.RESULT_ERROR);
    }

    public long getSpinnerValue(int parentId) {
        return getInternalSpinner(parentId).getSelectedItemId();
    }

    private AdapterView<Adapter> getInternalSpinner(int parentId) {
        return (AdapterView<Adapter>) (testObject.findViewById(parentId)
                .findViewById(R.id.config_spinner));
    }

    static class TestConfigActivity extends ConfigActivity {
        @Override
        protected void inject() {
            // No op
        }
    }
}
