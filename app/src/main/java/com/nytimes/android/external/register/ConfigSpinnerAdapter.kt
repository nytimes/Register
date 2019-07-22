package com.nytimes.android.external.register

import android.content.Context
import android.support.annotation.IntDef
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.*

class ConfigSpinnerAdapter(context: Context, items: List<ConfigResponse>) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val items: List<ConfigResponse> = ArrayList(items)

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].responseCode.toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val item = getItem(position) as ConfigResponse
        val configViewHolder = getViewHolder(view, viewGroup)
        configViewHolder.bind(item, MODE_SPINNER)
        return configViewHolder.itemView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position) as ConfigResponse
        val configViewHolder = getViewHolder(convertView, parent)
        configViewHolder.bind(item, MODE_DROP_DOWN)
        return configViewHolder.itemView
    }

    private fun getViewHolder(view: View?, viewGroup: ViewGroup): ConfigViewHolder {
        return if (view == null) {
            val itemView = inflater.inflate(R.layout.item_config, viewGroup, false)
            ConfigViewHolder(itemView)
        } else {
            view.tag as ConfigViewHolder
        }
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(MODE_SPINNER, MODE_DROP_DOWN)
    internal annotation class ViewMode

    companion object {
        const val MODE_SPINNER = 0
        const val MODE_DROP_DOWN = 1
    }
}
