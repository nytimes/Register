package com.nytimes.android.external.playbillingtester;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Wrapper for user preferences such as API overrides and purchases
 */

public class Purchases {

    static final int PAGE_LIMIT = 100; // not sure what this limit actually is
    private static final Logger LOGGER = LoggerFactory.getLogger(Purchases.class);
    private static final String GOOGLE_PURCHASE_ITEMS_SUB = "GoogleItems";
    private static final String GOOGLE_PURCHASE_ITEMS_IAP = "GoogleItemsIAP";

    @NonNull
    protected Gson gson;

    @NonNull
    private final SharedPreferences sharedPreferences;

    @NonNull
    private final Signer signer;

    @Inject
    public Purchases(@NonNull SharedPreferences sharedPreferences, @NonNull Gson gson, @NonNull Signer signer) {
        this.sharedPreferences = sharedPreferences;
        this.gson = gson;
        this.signer = signer;
    }


    void addPurchase(@NonNull String inAppPurchaseDataStr, @NonNull String itemType) {
        Set<String> items = sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<>());
        items.add(inAppPurchaseDataStr);
        sharedPreferences.edit().putStringSet(getItemsKeyFromType(itemType), items).apply();
    }

    public void removePurchase(@NonNull String inAppPurchaseDataStr, @NonNull String itemType) {
        Set<String> items = sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<String>());
        items.remove(inAppPurchaseDataStr);
        sharedPreferences.edit().putStringSet(getItemsKeyFromType(itemType), items).apply();
    }

    @NonNull
    Set<InAppPurchaseData> getInAppPurchaseData(@NonNull String itemType) {
        Set<InAppPurchaseData> ret = new LinkedHashSet<>();
        for (String json : sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<>())) {
            ret.add(gson.fromJson(json, InAppPurchaseData.class));
        }
        return ret;
    }

    @NonNull
    public PurchasesLists getPurchasesLists(@NonNull String type, @Nullable String continuationToken) {
        ImmutablePurchasesLists.Builder builder = ImmutablePurchasesLists.builder();
        ArrayList<InAppPurchaseData> data = new ArrayList(getInAppPurchaseData(type));
        int first = getFirst(continuationToken);
        int limit = Math.min(first + PAGE_LIMIT, data.size());
        for (int i = first; i < limit; i++) {
            String jsonData = gson.toJson(data.get(i), InAppPurchaseData.class);
            builder.addPurchaseItemList(data.get(i).productId());
            builder.addPurchaseDataList(jsonData);
            String signedData = "";
            try {
                signedData = signer.signData(jsonData);
            } catch (InvalidKeyException | SignatureException exception) {
                LOGGER.error("Exception signing purchase data", exception);
            }
            builder.addDataSignatureList(signedData);
        }
        if (limit < data.size()) {
            builder.continuationToken(String.format("%d", limit));
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

    @NonNull
    Optional<String> getReceiptForSku(@NonNull String sku, @NonNull String itemType) {
        Optional<String> receipt = Optional.absent();
        for (String json : sharedPreferences.getStringSet(getItemsKeyFromType(itemType), new LinkedHashSet<>())) {
            InAppPurchaseData inAppPurchaseData = gson.fromJson(json, InAppPurchaseData.class);
            if (inAppPurchaseData.productId().equals(sku)) {
                receipt = Optional.of(inAppPurchaseData.purchaseToken());
            }
        }
        return receipt;
    }

    void purgePurchases() {
        sharedPreferences.edit().remove(GOOGLE_PURCHASE_ITEMS_IAP).remove(GOOGLE_PURCHASE_ITEMS_SUB).apply();
    }

    @NonNull
    private String getItemsKeyFromType(@NonNull String type) {
        if (GoogleUtil.BILLING_TYPE_IAP.equals(type)) {
            return GOOGLE_PURCHASE_ITEMS_IAP;
        } else {
            return GOOGLE_PURCHASE_ITEMS_SUB;
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
