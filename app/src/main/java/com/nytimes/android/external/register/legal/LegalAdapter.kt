package com.nytimes.android.external.register.legal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nytimes.android.external.register.R
import java.util.*

/**
 * simple adapter to bind license items with viewHolder
 */
class LegalAdapter(private val inflater: LayoutInflater, items: List<Pair<String, String>>) : RecyclerView.Adapter<LegalViewHolder>() {

    private val items = ArrayList<Pair<String, String>>(items)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegalViewHolder {
        return LegalViewHolder(
                inflater.inflate(R.layout.nyt_register_item_legal, parent, false))
    }

    override fun onBindViewHolder(holder: LegalViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    fun getItem(position: Int): Pair<String, String> {
        return items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

}
