package com.nytimes.android.external.playbillingtester;


import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class ConfigSpinnerAdapter extends BaseAdapter {

    private static final int MODE_SPINNER = 0;
    private static final int MODE_DROP_DOWN = 1;

    private final LayoutInflater inflater;
    private final List<ConfigResponse> items;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_SPINNER, MODE_DROP_DOWN})
    private  @interface ViewMode {
    }

    ConfigSpinnerAdapter(@NonNull Context context, List<ConfigResponse> items) {
        this.inflater = LayoutInflater.from(context);
        this.items = new ArrayList<>(items);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).responseCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ConfigResponse item = (ConfigResponse) getItem(position);
        ConfigViewHolder configViewHolder = getViewHolder(view, viewGroup);
        configViewHolder.bind(item, MODE_SPINNER);
        return configViewHolder.itemView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ConfigResponse item = (ConfigResponse) getItem(position);
        ConfigViewHolder configViewHolder = getViewHolder(convertView, parent);
        configViewHolder.bind(item, MODE_DROP_DOWN);
        return configViewHolder.itemView;
    }

    private ConfigViewHolder getViewHolder(View view, ViewGroup viewGroup) {
        if (view == null) {
            View itemView = inflater.inflate(R.layout.item_config, viewGroup, false);
            return new ConfigViewHolder(itemView);
        } else {
            return (ConfigViewHolder) view.getTag();
        }
    }

    private static class ConfigViewHolder {

        private final View itemView;
        private final TextView codeView;
        private final TextView nameView;
        private final ImageView iconView;

        ConfigViewHolder(View v) {
            itemView = v;
            itemView.setTag(this);

            codeView = (TextView) itemView.findViewById(R.id.item_config_code);
            nameView = (TextView) itemView.findViewById(R.id.item_config_name);
            iconView = (ImageView) itemView.findViewById(R.id.item_config_down_icon);
        }

        void bind(ConfigResponse item, @ViewMode int mode) {
            Context context = itemView.getContext();
            int textColor = getCodeColor(context, item);
            codeView.setText(String.valueOf(item.responseCode()));
            codeView.setTextColor(textColor);
            nameView.setText(item.responseName());
            nameView.setTextColor(textColor);
            iconView.setVisibility(mode == MODE_DROP_DOWN ? View.GONE : View.VISIBLE);
        }

        @ColorInt
        private int getCodeColor(Context context, ConfigResponse item) {
            int colorResId;
            switch (item.responseId()){
                case "spn_ok":
                    colorResId = R.color.config_green;
                    break;
                case "spn_item_unavailable":
                case "spn_item_owned":
                case "spn_item_not_owned":
                    colorResId = R.color.config_yellow;
                    break;
                case "spn_dev_error":
                case "spn_error":
                    colorResId = R.color.config_red;
                    break;
                case "spn_billing_unavailable":
                    colorResId = R.color.config_blue;
                    break;
                case "spn_default":
                    colorResId = R.color.config_gray;
                    break;
                default:
                    throw new IllegalArgumentException("No color for ConfigResponse with id=" + item.responseId());
            }
            return ContextCompat.getColor(context, colorResId);
        }
    }
}
