package com.nytimes.android.external.register.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class EmptyItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;

    public EmptyItemDecoration(int size) {
        this.space = size;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = space;
        }
    }
}
