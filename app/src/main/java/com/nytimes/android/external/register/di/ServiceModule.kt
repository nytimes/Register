package com.nytimes.android.external.register.di

import android.app.Service
import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.nytimes.android.external.register.*
import com.nytimes.android.external.register.APIOverrides.Companion.CONFIG_FILE
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.register.model.Config
import dagger.Module
import dagger.Provides
import java.io.*
import java.nio.charset.Charset

@Module
class ServiceModule(private val service: Service) {

    @Provides
    @ScopeService
    internal fun provideService(): Service {
        return service
    }

    @Provides
    @ScopeService
    internal fun provideConfig(gson: Gson): Config? {
        if (PermissionHandler.hasPermission(service)) {
            try {
                return gson.fromJson(BufferedReader(InputStreamReader(FileInputStream(File(
                        Environment.getExternalStorageDirectory().path, CONFIG_FILE)), Charset.forName("UTF-8"))), Config::class.java)
            } catch (exc: FileNotFoundException) {
                Log.e("ServiceModule", service.getString(R.string.config_not_found), exc)
            }

        }
        return null
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
    internal fun provideBuyIntentBundleBuilder(context: Context,
                                               apiOverrides: APIOverrides): BuyIntentBundleBuilder {
        return BuyIntentBundleBuilder(context, apiOverrides)
    }

    @Provides
    @ScopeService
    internal fun provideSkuDetailsBundleBuilder(apiOverrides: APIOverrides,
                                                config: Config?): SkuDetailsBundleBuilder {
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
    internal fun provideBuyIntentToReplaceSkusBundleBuilder(context: Context,
                                                            apiOverrides: APIOverrides): BuyIntentToReplaceSkusBundleBuilder {
        return BuyIntentToReplaceSkusBundleBuilder(context, apiOverrides)
    }
}
