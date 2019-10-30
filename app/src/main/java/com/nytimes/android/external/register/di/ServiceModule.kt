package com.nytimes.android.external.register.di

import android.app.Service
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.nytimes.android.external.register.*
import com.nytimes.android.external.register.APIOverrides.Companion.CONFIG_FILE
import com.nytimes.android.external.register.bundle.*
import com.nytimes.android.external.register.model.Config
import dagger.Module
import dagger.Provides
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
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
        try {
            return gson.fromJson(BufferedReader(InputStreamReader(readConfigFile(), Charset.forName("UTF-8"))), Config::class.java)
        } catch (exc: FileNotFoundException) {
            Log.e("ServiceModule", service.getString(R.string.nyt_register_config_not_found), exc)
        }
        return null
    }

    private fun readConfigFile(): InputStream {
        return service.resources.assets.open(CONFIG_FILE)
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
