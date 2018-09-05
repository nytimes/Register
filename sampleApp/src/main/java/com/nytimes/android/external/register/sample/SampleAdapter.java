package com.nytimes.android.external.register.sample;

import android.content.Context;
import android.content.res.ColorStateList;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nytimes.android.external.registerlib.GoogleProductResponse;
import com.nytimes.android.external.registerlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;


public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {
    private final PrefsManager prefsManager;
    private final Map<String, InAppPurchaseData> purchasesMap;
    private final List<GoogleProductResponse> items;
    private final ColorStateList colorTesterEnbeled;
    private final ColorStateList colorTesterDisabled;

    private LayoutInflater inflater;
    private PublishSubject<GoogleProductResponse> publishSubject = PublishSubject.create();


    SampleAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        prefsManager = new PrefsManager(PreferenceManager.getDefaultSharedPreferences(context));
        purchasesMap = new HashMap<>();
        items = new ArrayList<>();
        colorTesterEnbeled = ContextCompat.getColorStateList(context, R.color.button_background_enabled);
        colorTesterDisabled = ContextCompat.getColorStateList(context, R.color.button_background_disabled);
    }

    @Override
    public SampleAdapter.SampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_card, parent, false);
        return new SampleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SampleAdapter.SampleViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        GoogleProductResponse item = items.get(position);
        boolean isPurchased = purchasesMap.containsKey(item.productId());

        holder.title.setText(item.title());
        holder.description.setText(item.description());
        holder.button.setText(isPurchased ? context.getString(R.string.purchased) :
                item.introductoryPrice() == null ? item.price() : item.introductoryPrice() + "(" + item.price() + ")");
        holder.button.setEnabled(!isPurchased);
        ViewCompat.setBackgroundTintList(holder.button, prefsManager.isUsingTestGoogleServiceProvider() ?
                colorTesterEnbeled : colorTesterDisabled);
        holder.button.setOnClickListener(v -> publishSubject.onNext(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void addItem(GoogleProductResponse item){
        this.items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    void addPurchase(InAppPurchaseData purchase){
        purchasesMap.put(purchase.productId(), purchase);
        for (GoogleProductResponse item : items) {
            if (item.productId().equals(purchase.productId())){
                notifyItemChanged(items.indexOf(item));
                break;
            }
        }
    }

    PublishSubject<GoogleProductResponse> getClickSubject() {
        return publishSubject;
    }

    void clear(){
        purchasesMap.clear();
        items.clear();
        notifyDataSetChanged();
    }

    void destroy(){
        if (publishSubject != null) {
            publishSubject.onComplete();
            publishSubject = null;
        }

        inflater = null;
        purchasesMap.clear();
        items.clear();
    }

    static class SampleViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView description;
        final Button button;

        SampleViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.item_card_title);
            description = (TextView) itemView.findViewById(R.id.item_card_description);
            button = (Button) itemView.findViewById(R.id.item_card_button);
        }
    }
}
