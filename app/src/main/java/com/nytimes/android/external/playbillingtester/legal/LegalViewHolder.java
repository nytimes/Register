package com.nytimes.android.external.playbillingtester.legal;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nytimes.android.external.playbillingtester.R;

import java.util.Map;

/**
 * it shows the license holder and license value
 */
public class LegalViewHolder extends RecyclerView.ViewHolder {

    @NonNull
    private final TextView title;
    @NonNull
    private final TextView body;

    public LegalViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.legal_item_title);
        body = itemView.findViewById(R.id.legal_item_body);
    }

    public void onBind(@NonNull Map.Entry<String, String> legalText) {
        title.setText(legalText.getKey());
        body.setText(legalText.getValue());
    }
}
