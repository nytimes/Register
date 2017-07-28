package com.nytimes.android.external.register.di;

import android.app.Application;
import android.app.Service;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.register.BillingServiceStubImpl;
import com.nytimes.android.external.register.IInAppBillingService;
import com.nytimes.android.external.register.PermissionHandler;
import com.nytimes.android.external.register.Purchases;
import com.nytimes.android.external.register.R;
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder;
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder;
import com.nytimes.android.external.register.bundle.ConsumePurchaseResponse;
import com.nytimes.android.external.register.bundle.PurchasesBundleBuilder;
import com.nytimes.android.external.register.bundle.SkuDetailsBundleBuilder;
import com.nytimes.android.external.register.model.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

import dagger.Module;
import dagger.Provides;

import static com.google.common.base.Charsets.UTF_8;
import static com.nytimes.android.external.register.APIOverrides.CONFIG_FILE;

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
    Optional<Config> provideConfig(Gson gson) {
        if (PermissionHandler.hasPermission(service)) {
            try {
                return Optional.of(gson.fromJson(Files.newReader(new File(
                        Environment.getExternalStorageDirectory().getPath(), CONFIG_FILE), UTF_8), Config.class));
            } catch (FileNotFoundException exc) {
                LOGGER.error(service.getString(R.string.config_not_found), exc);
            }
        }
        return Optional.absent();
    }

    @Provides
    @ScopeService
    IInAppBillingService.Stub provideBillingServiceStubImpl(APIOverrides apiOverrides,
                                                            BuyIntentBundleBuilder buyIntentBundleBuilder,
                                                            SkuDetailsBundleBuilder skuDetailsBundleBuilder,
                                                            PurchasesBundleBuilder purchasesBundleBuilder,
                                                            ConsumePurchaseResponse consumePurchaseResponse,
                                                            BuyIntentToReplaceSkusBundleBuilder
                                                                    buyIntentToReplaceSkusBundleBuilder) {
        return new BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, consumePurchaseResponse, buyIntentToReplaceSkusBundleBuilder);
    }

    @Provides
    @ScopeService
    BuyIntentBundleBuilder provideBuyIntentBundleBuilder(Application application,
                                                         APIOverrides apiOverrides) {
        return new BuyIntentBundleBuilder(application, apiOverrides);
    }

    @Provides
    @ScopeService
    SkuDetailsBundleBuilder provideSkuDetailsBundleBuilder(APIOverrides apiOverrides,
                                                           Optional<Config> config) {
        return new SkuDetailsBundleBuilder(apiOverrides, config);
    }

    @Provides
    @ScopeService
    PurchasesBundleBuilder providePurchasesBundleBuilder(APIOverrides apiOverrides, Purchases purchases) {
        return new PurchasesBundleBuilder(apiOverrides, purchases);
    }

    @Provides
    @ScopeService
    ConsumePurchaseResponse provideConsumePurchaseBundleBuilder(APIOverrides apiOverrides, Purchases purchases) {
        return new ConsumePurchaseResponse(apiOverrides, purchases);
    }

    @Provides
    @ScopeService
    BuyIntentToReplaceSkusBundleBuilder provideBuyIntentToReplaceSkusBundleBuilder(Application application,
                                                                                   APIOverrides apiOverrides) {
        return new BuyIntentToReplaceSkusBundleBuilder(application, apiOverrides);
    }
}
