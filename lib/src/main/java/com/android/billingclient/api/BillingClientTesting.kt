package com.android.billingclient.api

import android.annotation.TargetApi
import android.app.Activity
import android.content.*
import android.os.*
import java.io.FileDescriptor

/**
 * An implementation of [BillingClient] that acts as a man-in-the-middle for the actual
 * [BillingClientImpl] provided in the Play Billing Library
 * (see [BillingClientTesting.BillingClientTesting]).
 *
 *
 * Please note that this class **must** be placed in the
 * `com.android.billingclient.api` package, so to have visibility over the
 * [BillingClientImpl] class (which is package-private).
 */
class BillingClientTesting(context: Context, listener: PurchasesUpdatedListener) : BillingClient() {

    private val billingClientImpl: BillingClientImpl =
            BillingClientImpl(BillingContextWrapper(context), listener)

    override fun isReady(): Boolean {
        return billingClientImpl.isReady
    }

    override fun isFeatureSupported(feature: String): Int {
        return billingClientImpl.isFeatureSupported(feature)
    }

    override fun startConnection(listener: BillingClientStateListener) {
        billingClientImpl.startConnection(listener)
    }

    override fun endConnection() {
        billingClientImpl.endConnection()
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        return billingClientImpl.launchBillingFlow(activity, params)
    }

    override fun queryPurchases(skuType: String): Purchase.PurchasesResult {
        return billingClientImpl.queryPurchases(skuType)
    }

    override fun querySkuDetailsAsync(params: SkuDetailsParams, listener: SkuDetailsResponseListener) {
        billingClientImpl.querySkuDetailsAsync(params, listener)
    }

    override fun consumeAsync(purchaseToken: String, listener: ConsumeResponseListener) {
        billingClientImpl.consumeAsync(purchaseToken, listener)
    }

    override fun queryPurchaseHistoryAsync(skuType: String, listener: PurchaseHistoryResponseListener) {
        billingClientImpl.queryPurchaseHistoryAsync(skuType, listener)
    }

    /**
     * This class makes sure that we wrap the given [Context] and intercept the
     * [Context.bindService] method. In this method we make
     * sure to call super by passing a new [Intent] that points to the Register billing
     * service instead of Google's. We also need to wrap the [ServiceConnection], and we do
     * so thanks to [WrappedServiceConnection].
     */
    private class BillingContextWrapper(context: Context) : ContextWrapper(context.applicationContext) {

        override fun getApplicationContext(): Context {
            return this
        }

        override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
            if (service.action == WRAPPED_ACTION && service.getPackage() == WRAPPED_PACKAGE_NAME) {
                val originalComponent = service.component
                val intent = Intent(service)
                intent.action = INTENT_STRING
                intent.setPackage(INTENT_PKG)
                intent.component = null
                val intentServices = packageManager.queryIntentServices(intent, 0)
                val resolveInfo = intentServices[0]
                val packageName = resolveInfo.serviceInfo.packageName
                val className = resolveInfo.serviceInfo.name
                val component = ComponentName(packageName, className)
                intent.component = component
                return super.bindService(intent, WrappedServiceConnection(conn, originalComponent!!), flags)
            }

            return super.bindService(service, conn, flags)
        }

        companion object {
            private const val WRAPPED_PACKAGE_NAME = "com.android.vending"
            private const val WRAPPED_ACTION = "com.android.vending.billing.InAppBillingService.BIND"

            private const val INTENT_STRING = "com.nytimes.android.external.register.InAppBillingService.BIND"
            private const val INTENT_PKG = "com.nytimes.android.external.register"
        }
    }

    /**
     * This class wraps a [ServiceConnection] in order to being able to wrap the
     * [IBinder] parameter that is passed to
     * [ServiceConnection.onServiceConnected]
     * (see [WrappedBinder]).
     */
    private class WrappedServiceConnection internal constructor(private val wrappedServiceConnection: ServiceConnection,
                                                                private val originalComponent: ComponentName) : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            wrappedServiceConnection
                    .onServiceConnected(originalComponent, WrappedBinder(service))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            wrappedServiceConnection.onServiceDisconnected(originalComponent)
        }

        @TargetApi(Build.VERSION_CODES.O)
        override fun onBindingDied(name: ComponentName) {
            wrappedServiceConnection.onBindingDied(originalComponent)
        }
    }

    /**
     * This class wraps a [IBinder] instance in order to manipulate what the underlying
     * service will really be. To do so, we need to change all the entry points to point to the
     * Register service, and all the results to point back to the Play interface (see
     * [WrappedBinder.getInterfaceDescriptor],
     * [WrappedBinder.queryLocalInterface],
     * [WrappedBinder.transact]).
     */
    private class WrappedBinder internal constructor(private val binder: IBinder) : IBinder {

        private fun getRegisterDescriptor(interfaceDescriptor: String): String {
            return if (interfaceDescriptor == PLAY_DESCRIPTOR) {
                REGISTER_DESCRIPTOR
            } else {
                interfaceDescriptor
            }
        }

        private fun getPlayDescriptor(interfaceDescriptor: String?): String? {
            return if (interfaceDescriptor == REGISTER_DESCRIPTOR) {
                PLAY_DESCRIPTOR
            } else {
                interfaceDescriptor
            }
        }

        @Throws(RemoteException::class)
        override fun getInterfaceDescriptor(): String? {
            return getPlayDescriptor(binder.interfaceDescriptor)
        }

        override fun pingBinder(): Boolean {
            return binder.pingBinder()
        }

        override fun isBinderAlive(): Boolean {
            return binder.isBinderAlive
        }

        override fun queryLocalInterface(descriptor: String): IInterface? {
            return binder.queryLocalInterface(getRegisterDescriptor(descriptor))
        }

        @Throws(RemoteException::class)
        override fun dump(fd: FileDescriptor, args: Array<String>?) {
            binder.dump(fd, args)
        }

        @Throws(RemoteException::class)
        override fun dumpAsync(fd: FileDescriptor, args: Array<String>?) {
            binder.dumpAsync(fd, args)
        }

        @Throws(RemoteException::class)
        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val newParcel = Parcel.obtain()
            try {
                val currentPosition = data.dataPosition()
                data.setDataPosition(0)
                data.enforceInterface(PLAY_DESCRIPTOR)
                val dataOffset = data.dataPosition()
                val dataLength = data.dataAvail()
                data.setDataPosition(currentPosition)
                newParcel.writeInterfaceToken(REGISTER_DESCRIPTOR)
                newParcel.appendFrom(data, dataOffset, dataLength)
                return binder.transact(code, newParcel, reply, flags)
            } finally {
                newParcel.recycle()
            }
        }

        @Throws(RemoteException::class)
        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
            binder.linkToDeath(recipient, flags)
        }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
            return binder.unlinkToDeath(recipient, flags)
        }

        companion object {
            private const val REGISTER_DESCRIPTOR = "com.nytimes.android.external.register.IInAppBillingService"
            private const val PLAY_DESCRIPTOR = "com.android.vending.billing.IInAppBillingService"
        }
    }
}
