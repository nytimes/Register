package com.nytimes.android.external.playbillingtester;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.nytimes.android.external.playbillingtesterlib.InAppPurchaseData;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private OnItemCallback callback;
    private LayoutInflater inflater;
    private final List<InAppPurchaseData> items;

    MainAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        items = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        InAppPurchaseData item = items.get(position);

        holder.productTitle.setText(item.purchaseToken());
        holder.productTypeView.setText(item.productId());
        holder.purchaseDateView.setText(getTimeLapsed(item.purchaseTime()));
        holder.contentView.setOnClickListener(v -> {
            if (holder.swipeLayout.getDragDistance() == 0){
                if (callback != null){
                    callback.onItemClicked(item);
                }
            } else {
                holder.swipeLayout.close(true);
            }
        });
        holder.delete.setOnClickListener(callback == null ?
                null : v -> callback.onItemDeleted(item));
    }

    private CharSequence getTimeLapsed(String timeString){
        long purchaseTime = Long.parseLong(timeString);
        return DateUtils.getRelativeTimeSpanString(purchaseTime,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    void setItems(List<InAppPurchaseData> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    void destroy(){
        callback = null;
        inflater = null;
        items.clear();
    }

    void setCallback(OnItemCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onViewRecycled(MainViewHolder holder) {
        super.onViewRecycled(holder);
        holder.swipeLayout.close(false, false);
    }

    static class MainViewHolder extends RecyclerView.ViewHolder {

        private final SwipeLayout swipeLayout;
        private final View contentView;
        private final View bottomView;
        private final TextView productTitle;
        private final TextView productTypeView;
        private final TextView purchaseDateView;
        private final View delete;

        MainViewHolder(View itemView) {
            super(itemView);

            contentView = itemView.findViewById(R.id.item_main_top);
            bottomView = itemView.findViewById(R.id.item_main_bottom);
            productTitle = (TextView) itemView.findViewById(R.id.item_product_name);
            productTypeView = (TextView) itemView.findViewById(R.id.item_product_type);
            purchaseDateView = (TextView) itemView.findViewById(R.id.item_product_purchase_date);
            delete = itemView.findViewById(R.id.item_product_delete);

            swipeLayout = (SwipeLayout) itemView;
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomView);
        }
    }

    interface OnItemCallback {
        void onItemClicked(InAppPurchaseData item);
        void onItemDeleted(InAppPurchaseData item);
    }
}
