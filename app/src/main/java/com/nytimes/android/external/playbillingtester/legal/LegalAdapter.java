package com.nytimes.android.external.playbillingtester.legal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nytimes.android.external.playbillingtester.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * simple adapter to bind license items with viewHolder
 */
public class LegalAdapter extends RecyclerView.Adapter<LegalViewHolder> {

    @NonNull
    private final List<Map.Entry<String, String>> items;
    @NonNull
    private final LayoutInflater inflater;

    LegalAdapter(@NonNull Context context, List<Map.Entry<String, String>> licenseList) {
        inflater = LayoutInflater.from(context);
        items = new ArrayList<>(licenseList);
    }

    @Override
    public LegalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LegalViewHolder(
                inflater.inflate(R.layout.item_legal, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LegalViewHolder holder, int position) {
        holder.onBind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
