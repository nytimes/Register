package com.nytimes.android.external.register;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.nytimes.android.external.registerlib.InAppPurchaseData;

import org.immutables.value.Value;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import static com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_IAP;
import static com.nytimes.android.external.registerlib.GoogleUtil.BILLING_TYPE_SUBSCRIPTION;

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

public class Purchases {

    static final int PAGE_LIMIT = 100; // not sure what this limit actually is

    @NonNull
    private final SharedPreferences sharedPreferences;

    @NonNull
    private final Signer signer;

    @Inject
    public Purchases(@NonNull SharedPreferences sharedPreferences, @NonNull Signer signer) {
        this.sharedPreferences = sharedPreferences;
        this.signer = signer;
    }


    @NonNull
    public PurchasesLists getPurchasesLists(@NonNull String type, @Nullable String continuationToken) {
        ImmutablePurchasesLists.Builder builder = ImmutablePurchasesLists.builder();
        ArrayList<InAppPurchaseData> data = new ArrayList(getInAppPurchaseData(type));
        int first = getFirst(continuationToken);
        int limit = Math.min(first + PAGE_LIMIT, data.size());
        for (int i = first; i < limit; i++) {
            String jsonData = InAppPurchaseData.toJson(data.get(i));
            builder.addPurchaseItemList(data.get(i).productId());
            builder.addPurchaseDataList(jsonData);
            String signedData = "";
            try {
                signedData = signer.signData(jsonData);
            } catch (InvalidKeyException | SignatureException exception) {
                Log.e("Purchases", "Exception signing purchase data", exception);
            }
            builder.addDataSignatureList(signedData);
        }
        if (limit < data.size()) {
            builder.continuationToken(String.format("%d", limit));
        }
        return builder.build();
    }

    boolean addPurchase(@NonNull String inAppPurchaseDataStr, @NonNull String itemType) {
        Set<String> items = getPurchases(itemType);
        if (items.contains(inAppPurchaseDataStr)) {
            return false;
        } else {
            Set<String> toAdd =  new ImmutableSet.Builder<String>().addAll(items).add(inAppPurchaseDataStr).build();
            sharedPreferences.edit().putStringSet(itemType, toAdd).apply();
            return true;
        }
    }

    public boolean removePurchase(@NonNull String inAppPurchaseDataStr, @NonNull String itemType) {
        Set<String> items = getPurchases(itemType);
        boolean removed = items.remove(inAppPurchaseDataStr);
        if (removed) {
            sharedPreferences.edit().putStringSet(itemType, items).apply();
        }
        return removed;
    }

    boolean replacePurchase(@NonNull String newInAppPurchaseDataStr, List<String> replacedSkus) {
        Set<String> purchasedItems = getPurchases(BILLING_TYPE_SUBSCRIPTION);
        Set<String> purchasedSkus = getPurchasedSkus(BILLING_TYPE_SUBSCRIPTION);
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        if (purchasedSkus.containsAll(replacedSkus) && !purchasedItems.contains(newInAppPurchaseDataStr)) {
            Set<String> finalSet = builder
                    .addAll(getPurchasesExceptForSkus(BILLING_TYPE_SUBSCRIPTION, ImmutableSet.copyOf(replacedSkus)))
                    .add(newInAppPurchaseDataStr)
                    .build();
            sharedPreferences.edit().putStringSet(BILLING_TYPE_SUBSCRIPTION,
                    finalSet).commit();
            return true;
        }
        return  false;
    }

    @NonNull
    Set<InAppPurchaseData> getInAppPurchaseData(@NonNull String itemType) {
        Set<InAppPurchaseData> ret = new LinkedHashSet<>();
        for (String json : getPurchases(itemType)) {
            ret.add(InAppPurchaseData.fromJson(json));
        }
        return ret;
    }

    @NonNull
    Set<String> getReceiptsForSkus(@NonNull Set<String> skus, @NonNull String itemType) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String json : getPurchases(itemType)) {
            InAppPurchaseData inAppPurchaseData = InAppPurchaseData.fromJson(json);
            if (skus.contains(inAppPurchaseData.productId())) {
                builder.add(inAppPurchaseData.purchaseToken());
            }
        }
        return builder.build();
    }

    void purgePurchases() {
        sharedPreferences.edit().remove(BILLING_TYPE_IAP).remove(BILLING_TYPE_SUBSCRIPTION).apply();
    }

    private Set<String> getPurchases(@NonNull String itemType) {
        return sharedPreferences.getStringSet(itemType, ImmutableSet.of());
    }

    private Set<String> getPurchasesExceptForSkus(@NonNull String itemType, @NonNull Set<String> skuFilter) {
        return ImmutableSet.copyOf(Collections2.filter(getPurchases(itemType),
                json -> !skuFilter.contains(InAppPurchaseData.fromJson(json).productId())));
    }

    private Set<String> getPurchasedSkus(String itemType) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (String inAppPurchaseDataStr : getPurchases(itemType)) {
            builder.add(InAppPurchaseData.fromJson(inAppPurchaseDataStr).productId());
        }
        return builder.build();
    }

    private int getFirst(String continuationToken) {
        try {
            return Integer.parseInt(continuationToken);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Value.Immutable
    public interface PurchasesLists {
        List<String> purchaseItemList();
        List<String> purchaseDataList();
        List<String> dataSignatureList();
        @Nullable
        String continuationToken();
    }
}
