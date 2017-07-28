package com.nytimes.android.external.register.legal;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nytimes.android.external.register.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * simple adapter to bind license items with viewHolder
 */
public class LegalAdapter extends RecyclerView.Adapter<com.nytimes.android.external.register.legal.LegalViewHolder> {

    @NonNull
    private final List<Map.Entry<String, String>> items;
    @NonNull
    private final LayoutInflater inflater;

    LegalAdapter(@NonNull LayoutInflater inflater, @NonNull List<Map.Entry<String, String>> items) {
        this.inflater = inflater;
        this.items = new ArrayList<>(items);
    }

    @Override
    public LegalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new com.nytimes.android.external.register.legal.LegalViewHolder(
                inflater.inflate(R.layout.item_legal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LegalViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    Map.Entry<String, String> getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
