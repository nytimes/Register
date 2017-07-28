package com.nytimes.android.external.register.bundle;

import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.Purchases;
import com.nytimes.android.external.registerlib.GoogleUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchasesBundleBuilderTest {

    private PurchasesBundleBuilder testObject;

    @Mock
    private APIOverrides apiOverrides;

    @Mock
    private Purchases purchases;

    @Mock
    private Purchases.PurchasesLists purchasesLists;

    private final String type = GoogleUtil.BILLING_TYPE_SUBSCRIPTION;
    private final List<String> purchasesDataList = ImmutableList.of("purchase1Data", "purchase2Data");
    private final List<String> purchaseItemList = ImmutableList.of("item1", "item2");
    private final List<String> signedPurchaseList = ImmutableList.of("signed1", "signed2");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObject = new PurchasesBundleBuilder(apiOverrides, purchases);
    }

    @Test
    public void testBundleOKNoContinuationToken() {
        when(apiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(purchases.getPurchasesLists(type, null)).thenReturn(purchasesLists);
        when(purchasesLists.purchaseDataList()).thenReturn(purchasesDataList);
        when(purchasesLists.purchaseItemList()).thenReturn(purchaseItemList);
        when(purchasesLists.dataSignatureList()).thenReturn(signedPurchaseList);
        Bundle bundle = testObject.newBuilder()
                .type(type)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(purchasesDataList);
        assertThat(bundle.getString(GoogleUtil.INAPP_CONTINUATION_TOKEN)).isNull();
    }

    @Test
    public void testBundleOKWithContinuationToken() {
        String continuationToken = "100";
        when(apiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_OK);
        when(purchases.getPurchasesLists(type, continuationToken)).thenReturn(purchasesLists);
        when(purchasesLists.purchaseDataList()).thenReturn(purchasesDataList);
        when(purchasesLists.continuationToken()).thenReturn(continuationToken);
        Bundle bundle = testObject.newBuilder()
                .type(type)
                .continuationToken(continuationToken)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_OK);
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isEqualTo(purchasesDataList);
        assertThat(bundle.getString(GoogleUtil.INAPP_CONTINUATION_TOKEN)).isEqualTo(continuationToken);
    }

    @Test
    public void testBundleNotOK() {
        when(apiOverrides.getGetPurchasesResponse()).thenReturn(GoogleUtil.RESULT_ERROR);

        Bundle bundle = testObject.newBuilder()
                .type(type)
                .build();

        assertThat(bundle.getInt(GoogleUtil.RESPONSE_CODE))
                .isEqualTo(GoogleUtil.RESULT_ERROR);
        assertThat(bundle.getStringArrayList(GoogleUtil.INAPP_PURCHASE_DATA_LIST))
                .isNull();
    }

    @Test
    public void testRawResponseCode() {
        testObject.rawResponseCode();
        verify(apiOverrides).getGetPurchasesResponse();
    }
}

