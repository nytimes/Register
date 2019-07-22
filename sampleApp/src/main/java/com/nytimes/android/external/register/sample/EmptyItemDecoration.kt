package com.nytimes.android.external.register.sample

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class EmptyItemDecoration(private val verticalSize: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State?) {
        if (parent.getChildAdapterPosition(view) != parent.adapter.itemCount - 1) {
            outRect.bottom = verticalSize
        }
    }
}
