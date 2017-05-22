package com.nytimes.android.external.playbillingtester.di;

import android.app.Application;
import android.app.Service;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtester.BillingServiceStubImpl;
import com.nytimes.android.external.playbillingtester.IInAppBillingService;
import com.nytimes.android.external.playbillingtester.PermissionHandler;
import com.nytimes.android.external.playbillingtester.R;
import com.nytimes.android.external.playbillingtester.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.ConsumePurchaseBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.PurchasesBundleBuilder;
import com.nytimes.android.external.playbillingtester.bundle.SkuDetailsBundleBuilder;
import com.nytimes.android.external.playbillingtester.model.Config;
import com.nytimes.android.external.playbillingtester.model.ImmutableConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

import dagger.Module;
import dagger.Provides;

import static com.google.common.base.Charsets.UTF_8;
import static com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases.CONFIG_FILE;

@Module
public class ServiceModule {

    static final Logger LOGGER = LoggerFactory.getLogger(ServiceModule.class);
    private final Service service;

    public ServiceModule(@NonNull Service service) {
        this.service = service;
    }

    @Provides
    @ScopeService
    Service provideService() {
        return service;
    }

    @Provides
    @ScopeService
    Config provideConfig(Gson gson) {
        if (PermissionHandler.hasPermission(service)) {
            try {
                return gson.fromJson(Files.newReader(new File(Environment.getExternalStorageDirectory().getPath(),
                        CONFIG_FILE), UTF_8), Config.class);
            } catch (FileNotFoundException exc) {
                LOGGER.error(service.getString(R.string.config_not_found), exc);
            }
        }
        return ImmutableConfig.builder().build();
    }

    @Provides
    @ScopeService
    IInAppBillingService.Stub provideBillingServiceStubImpl(APIOverridesAndPurchases apiOverridesAndPurchases,
                                                            Gson gson, Config config,
                                                            BuyIntentBundleBuilder buyIntentBundleBuilder,
                                                            SkuDetailsBundleBuilder skuDetailsBundleBuilder,
                                                            PurchasesBundleBuilder purchasesBundleBuilder,
                                                            ConsumePurchaseBundleBuilder consumePurchaseBundleBuilder) {
        return new BillingServiceStubImpl(apiOverridesAndPurchases, gson, config, buyIntentBundleBuilder,
                skuDetailsBundleBuilder, purchasesBundleBuilder, consumePurchaseBundleBuilder);
    }

    @Provides
    @ScopeService
    BuyIntentBundleBuilder provideBuyIntentBundleBuilder(Application application,
                                                         APIOverridesAndPurchases apiOverridesAndPurchases) {
        return new BuyIntentBundleBuilder(application, apiOverridesAndPurchases);
    }

    @Provides
    @ScopeService
    SkuDetailsBundleBuilder provideSkuDetailsBundleBuilder(APIOverridesAndPurchases apiOverridesAndPurchases,
                                                           Config config, Gson gson) {
        return new SkuDetailsBundleBuilder(apiOverridesAndPurchases, config, gson);
    }

    @Provides
    @ScopeService
    PurchasesBundleBuilder providePurchasesBundleBuilder(APIOverridesAndPurchases apiOverridesAndPurchases) {
        return new PurchasesBundleBuilder(apiOverridesAndPurchases);
    }

    @Provides
    @ScopeService
    ConsumePurchaseBundleBuilder provideConsumePurchaseBundleBuilder(APIOverridesAndPurchases apiOverridesAndPurchases) {
        return new ConsumePurchaseBundleBuilder(apiOverridesAndPurchases);
    }
}
