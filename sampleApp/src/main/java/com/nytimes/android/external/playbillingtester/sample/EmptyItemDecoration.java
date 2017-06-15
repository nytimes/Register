package com.nytimes.android.external.playbillingtester.sample;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class EmptyItemDecoration extends RecyclerView.ItemDecoration {

    private final int verticalSize;

    public EmptyItemDecoration(int size) {
        this.verticalSize = size;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = verticalSize;
        }
    }
}
