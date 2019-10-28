package com.nytimes.android.external.register

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val productTitle: TextView = itemView.findViewById(R.id.nyt_register_item_product_name)
    val productTypeView: TextView = itemView.findViewById(R.id.nyt_register_item_product_type)
    val purchaseDateView: TextView = itemView.findViewById(R.id.nyt_register_item_product_purchase_date)
    val deletePurchase: View = itemView.findViewById(R.id.nyt_register_item_product_delete)

}