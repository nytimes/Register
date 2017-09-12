package com.nytimes.android.external.register.products;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nytimes.android.external.register.BuyActivity;
import com.nytimes.android.external.register.R;
import com.nytimes.android.external.register.model.ConfigSku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.subjects.PublishSubject;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private final LayoutInflater inflater;
    private final List<ConfigSku> items;
    private final PublishSubject<ConfigSku> clickSubject = PublishSubject.create();

    ProductsAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        items = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ConfigSku item = items.get(position);

        holder.productTitle.setText(item.title());
        holder.productSummary.setText(item.description());
        holder.productPrice.setText(String.format(BuyActivity.PRICE_FMT, item.price()));
        holder.itemView.setOnClickListener(v -> clickSubject.onNext(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    void setItems(Collection<ConfigSku> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    PublishSubject<ConfigSku> getClickSubject() {
        return clickSubject;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        final TextView productTitle;
        final TextView productPrice;
        final TextView productSummary;

        ProductViewHolder(View itemView) {
            super(itemView);

            productTitle = itemView.findViewById(R.id.item_title);
            productPrice = itemView.findViewById(R.id.item_price);
            productSummary = itemView.findViewById(R.id.item_summary);
        }
    }
}
