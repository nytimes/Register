package com.nytimes.android.external.register.legal

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.nytimes.android.external.register.R

/**
 * it shows the license holder and license value
 */
class LegalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val title: TextView = itemView.findViewById(R.id.legal_item_title)
    private val body: TextView = itemView.findViewById(R.id.legal_item_body)

    fun onBind(legalText: Pair<String, String>) {
        title.text = legalText.first
        body.text = legalText.second
    }
}
