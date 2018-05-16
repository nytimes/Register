package com.nytimes.android.external.register.bundle;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.BuyActivity;
import com.nytimes.android.external.registerlib.GoogleUtil;

import javax.inject.Inject;

public class BuyIntentBundleBuilder extends BaseBundleBuilder {

    public static final String EX_PACKAGE_NAME = "packageName";
    public static final String EX_SKU = "sku";
    public static final String EX_ITEM_TYPE = "type";
    public static final String EX_DEVELOPER_PAYLOAD = "developerPayload";

    protected final Application application;

    protected Intent intent;

    @Inject
    public BuyIntentBundleBuilder(Application application, APIOverrides apiOverrides) {
        super(apiOverrides);
        this.application = application;
    }

    public BuyIntentBundleBuilder newBuilder() {
        bundle = new Bundle();
        intent = new Intent(application, BuyActivity.class);
        return this;
    }

    public BuyIntentBundleBuilder packageName(String packageName) {
        intent.putExtra(EX_PACKAGE_NAME, packageName);
        return this;
    }

    public BuyIntentBundleBuilder sku(String sku) {
        intent.putExtra(EX_SKU, sku);
        return this;
    }

    public BuyIntentBundleBuilder type(String type) {
        intent.putExtra(EX_ITEM_TYPE, type);
        return this;
    }

    public BuyIntentBundleBuilder developerPayload(String developerPayload) {
        intent.putExtra(EX_DEVELOPER_PAYLOAD, developerPayload);
        return this;
    }

    public Bundle build() {
        int responseCode = responseCode();
        bundle.putInt(GoogleUtil.RESPONSE_CODE, responseCode);

        if (responseCode == GoogleUtil.RESULT_OK) {
            PendingIntent pendingIntent = PendingIntent.getActivity(application, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            bundle.putParcelable(GoogleUtil.BUY_INTENT, pendingIntent);
        }
        return bundle;
    }

    @Override
    protected int rawResponseCode() {
        return apiOverrides.getGetBuyIntentResponse();
    }
}
