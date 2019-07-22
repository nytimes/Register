package com.nytimes.android.external.register

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val productTitle : TextView = itemView.findViewById(R.id.item_product_name)
    val productTypeView : TextView = itemView.findViewById(R.id.item_product_type)
    val purchaseDateView : TextView = itemView.findViewById(R.id.item_product_purchase_date)
    val deletePurchase: View = itemView.findViewById(R.id.item_product_delete)

}