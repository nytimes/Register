package com.nytimes.android.external.register.di

import android.app.Application
import android.app.Service
import android.os.Environment
import android.util.Log
import com.google.common.base.Charsets.UTF_8
import com.google.common.base.Optional
import com.google.common.io.Files
import com.google.gson.Gson
import com.nytimes.android.external.register.*
import com.nytimes.android.external.register.APIOverrides.Companion.CONFIG_FILE
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.register.model.Config
import dagger.Module
import dagger.Provides
import java.io.File
import java.io.FileNotFoundException

@Module
class ServiceModule(private val service: Service) {

    @Provides
    @ScopeService
    internal fun provideService(): Service {
        return service
    }

    @Provides
    @ScopeService
    internal fun provideConfig(gson: Gson): Optional<Config> {
        if (PermissionHandler.hasPermission(service)) {
            try {
                return Optional.of(gson.fromJson(Files.newReader(File(
                        Environment.getExternalStorageDirectory().path, CONFIG_FILE), UTF_8), Config::class.java))
            } catch (exc: FileNotFoundException) {
                Log.e("ServiceModule", service.getString(R.string.config_not_found), exc)
            }

        }
        return Optional.absent()
    }

    @Provides
    @ScopeService
    internal fun provideBillingServiceStubImpl(apiOverrides: APIOverrides,
                                               buyIntentBundleBuilder: BuyIntentBundleBuilder,
                                               skuDetailsBundleBuilder: SkuDetailsBundleBuilder,
                                               purchasesBundleBuilder: PurchasesBundleBuilder,
                                               consumePurchaseResponse: ConsumePurchaseResponse,
                                               buyIntentToReplaceSkusBundleBuilder: BuyIntentToReplaceSkusBundleBuilder): IInAppBillingService.Stub {
        return BillingServiceStubImpl(apiOverrides, buyIntentBundleBuilder, skuDetailsBundleBuilder,
                purchasesBundleBuilder, consumePurchaseResponse, buyIntentToReplaceSkusBundleBuilder)
    }

    @Provides
    @ScopeService
    internal fun provideBuyIntentBundleBuilder(application: Application,
                                               apiOverrides: APIOverrides): BuyIntentBundleBuilder {
        return BuyIntentBundleBuilder(application, apiOverrides)
    }

    @Provides
    @ScopeService
    internal fun provideSkuDetailsBundleBuilder(apiOverrides: APIOverrides,
                                                config: Optional<Config>): SkuDetailsBundleBuilder {
        return SkuDetailsBundleBuilder(apiOverrides, config)
    }

    @Provides
    @ScopeService
    internal fun providePurchasesBundleBuilder(apiOverrides: APIOverrides, purchases: Purchases): PurchasesBundleBuilder {
        return PurchasesBundleBuilder(apiOverrides, purchases)
    }

    @Provides
    @ScopeService
    internal fun provideConsumePurchaseBundleBuilder(apiOverrides: APIOverrides, purchases: Purchases): ConsumePurchaseResponse {
        return ConsumePurchaseResponse(apiOverrides, purchases)
    }

    @Provides
    @ScopeService
    internal fun provideBuyIntentToReplaceSkusBundleBuilder(application: Application,
                                                            apiOverrides: APIOverrides): BuyIntentToReplaceSkusBundleBuilder {
        return BuyIntentToReplaceSkusBundleBuilder(application, apiOverrides)
    }
}
