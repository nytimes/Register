package com.android.billingclient.api;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileDescriptor;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@link BillingClient} that acts as a man-in-the-middle for the actual
 * {@link BillingClientImpl} provided in the Play Billing Library
 * (see {@link BillingClientTesting#BillingClientTesting(Context, PurchasesUpdatedListener)}).
 * <p>
 * Please note that this class <b>must</b> be placed in the
 * <code>com.android.billingclient.api</code> package, so to have visibility over the
 * {@link BillingClientImpl} class (which is package-private).
 */
public class BillingClientTesting extends BillingClient {

    private final BillingClientImpl billingClientImpl;

    public BillingClientTesting(Context context, PurchasesUpdatedListener listener) {
        final Context wrappedContext = new BillingContextWrapper(context);
        billingClientImpl = new BillingClientImpl(wrappedContext, listener);
    }

    @Override
    public int isFeatureSupported(String feature) {
        return billingClientImpl.isFeatureSupported(feature);
    }

    @Override
    public boolean isReady() {
        return billingClientImpl.isReady();
    }

    @Override
    public void startConnection(@NonNull BillingClientStateListener listener) {
        billingClientImpl.startConnection(listener);
    }

    @Override
    public void endConnection() {
        billingClientImpl.endConnection();
    }

    @Override
    public int launchBillingFlow(Activity activity, BillingFlowParams params) {
        return billingClientImpl.launchBillingFlow(activity, params);
    }

    @Override
    public Purchase.PurchasesResult queryPurchases(String skuType) {
        return billingClientImpl.queryPurchases(skuType);
    }

    @Override
    public void querySkuDetailsAsync(SkuDetailsParams params, SkuDetailsResponseListener listener) {
        billingClientImpl.querySkuDetailsAsync(params, listener);
    }

    @Override
    public void consumeAsync(String purchaseToken, ConsumeResponseListener listener) {
        billingClientImpl.consumeAsync(purchaseToken, listener);
    }

    @Override
    public void queryPurchaseHistoryAsync(String skuType, PurchaseHistoryResponseListener listener) {
        billingClientImpl.queryPurchaseHistoryAsync(skuType, listener);
    }

    /**
     * This class makes sure that we wrap the given {@link Context} and intercept the
     * {@link Context#bindService(Intent, ServiceConnection, int)} method. In this method we make
     * sure to call super by passing a new {@link Intent} that points to the Register billing
     * service instead of Google's. We also need to wrap the {@link ServiceConnection}, and we do
     * so thanks to {@link WrappedServiceConnection}.
     */
    private static class BillingContextWrapper extends ContextWrapper {
        private static final String WRAPPED_PACKAGE_NAME = "com.android.vending";
        private static final String WRAPPED_ACTION = "com.android.vending.billing.InAppBillingService.BIND";

        private static final String INTENT_STRING = "com.nytimes.android.external.register.InAppBillingService.BIND";
        private static final String INTENT_PKG = "com.nytimes.android.external.register";

        public BillingContextWrapper(Context context) {
            super(context.getApplicationContext());
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }

        @Override
        public boolean bindService(Intent service, ServiceConnection conn, int flags) {
            if (Objects.equals(service.getAction(), WRAPPED_ACTION)
                    && Objects.equals(service.getPackage(), WRAPPED_PACKAGE_NAME)) {
                final ComponentName originalComponent = service.getComponent();
                Intent intent = new Intent(service);
                intent.setAction(INTENT_STRING);
                intent.setPackage(INTENT_PKG);
                intent.setComponent(null);
                List<ResolveInfo> intentServices =
                        getPackageManager().queryIntentServices(intent, 0);
                ResolveInfo resolveInfo = intentServices.get(0);
                String packageName = resolveInfo.serviceInfo.packageName;
                String className = resolveInfo.serviceInfo.name;
                ComponentName component = new ComponentName(packageName, className);
                intent.setComponent(component);
                return super.bindService(intent, new WrappedServiceConnection(conn, originalComponent), flags);
            }

            return super.bindService(service, conn, flags);
        }
    }

    /**
     * This class wraps a {@link ServiceConnection} in order to being able to wrap the
     * {@link IBinder} parameter that is passed to
     * {@link ServiceConnection#onServiceConnected(ComponentName, IBinder)}
     * (see {@link WrappedBinder}).
     */
    private static class WrappedServiceConnection implements ServiceConnection {

        @NonNull
        private final ServiceConnection wrappedServiceConnection;

        @NonNull
        private final ComponentName originalComponent;

        WrappedServiceConnection(@NonNull ServiceConnection wrappedServiceConnection,
                                 @NonNull ComponentName originalComponent) {
            this.wrappedServiceConnection = wrappedServiceConnection;
            this.originalComponent = originalComponent;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            wrappedServiceConnection
                    .onServiceConnected(originalComponent, new WrappedBinder(service));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            wrappedServiceConnection.onServiceDisconnected(originalComponent);
        }

        @TargetApi(Build.VERSION_CODES.O)
        @Override
        public void onBindingDied(ComponentName name) {
            wrappedServiceConnection.onBindingDied(originalComponent);
        }
    }

    /**
     * This class wraps a {@link IBinder} instance in order to manipulate what the underlying
     * service will really be. To do so, we need to change all the entry points to point to the
     * Register service, and all the results to point back to the Play interface (see
     * {@link WrappedBinder#getInterfaceDescriptor()},
     * {@link WrappedBinder#queryLocalInterface(String)},
     * {@link WrappedBinder#transact(int, Parcel, Parcel, int)}).
     */
    private static class WrappedBinder implements IBinder {

        @NonNull
        private static final String REGISTER_DESCRIPTOR = "com.nytimes.android.external.register.IInAppBillingService";

        @NonNull
        private static final String PLAY_DESCRIPTOR = "com.android.vending.billing.IInAppBillingService";

        @NonNull
        private final IBinder binder;

        WrappedBinder(@NonNull IBinder binder) {
            this.binder = binder;
        }

        private String getRegisterDescriptor(String interfaceDescriptor) {
            if (Objects.equals(interfaceDescriptor, PLAY_DESCRIPTOR)) {
                return REGISTER_DESCRIPTOR;
            } else {
                return interfaceDescriptor;
            }
        }

        private String getPlayDescriptor(String interfaceDescriptor) {
            if (Objects.equals(interfaceDescriptor, REGISTER_DESCRIPTOR)) {
                return PLAY_DESCRIPTOR;
            } else {
                return interfaceDescriptor;
            }
        }

        @Nullable
        @Override
        public String getInterfaceDescriptor() throws RemoteException {
            return getPlayDescriptor(binder.getInterfaceDescriptor());
        }

        @Override
        public boolean pingBinder() {
            return binder.pingBinder();
        }

        @Override
        public boolean isBinderAlive() {
            return binder.isBinderAlive();
        }

        @Nullable
        @Override
        public IInterface queryLocalInterface(@NonNull String descriptor) {
            return binder.queryLocalInterface(getRegisterDescriptor(descriptor));
        }

        @Override
        public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            binder.dump(fd, args);
        }

        @Override
        public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
            binder.dumpAsync(fd, args);
        }

        @Override
        public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags)
                throws RemoteException {
            final Parcel newParcel = Parcel.obtain();
            try {
                final int currentPosition = data.dataPosition();
                data.setDataPosition(0);
                data.enforceInterface(PLAY_DESCRIPTOR);
                final int dataOffset = data.dataPosition();
                final int dataLength = data.dataAvail();
                data.setDataPosition(currentPosition);
                newParcel.writeInterfaceToken(REGISTER_DESCRIPTOR);
                newParcel.appendFrom(data, dataOffset, dataLength);
                return binder.transact(code, newParcel, reply, flags);
            } finally {
                newParcel.recycle();
            }
        }

        @Override
        public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
            binder.linkToDeath(recipient, flags);
        }

        @Override
        public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
            return binder.unlinkToDeath(recipient, flags);
        }
    }
}
