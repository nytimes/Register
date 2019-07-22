package com.nytimes.android.external.register

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView

internal class ConfigViewHolder(val itemView: View) {

    private val codeView: TextView = itemView.findViewById(R.id.item_config_code)
    private val nameView: TextView = itemView.findViewById(R.id.item_config_name)
    private val iconView: ImageView = itemView.findViewById(R.id.item_config_down_icon)

    init {
        itemView.tag = this
    }

    fun bind(item: ConfigResponse, @ConfigSpinnerAdapter.ViewMode mode: Int) {
        val context = itemView.context
        val textColor = getCodeColor(context, item)

        codeView.text = item.responseCode.toString()
        codeView.setTextColor(textColor)

        nameView.text = item.responseName
        nameView.setTextColor(textColor)

        iconView.visibility = if (mode == ConfigSpinnerAdapter.MODE_DROP_DOWN) View.GONE else View.VISIBLE
    }

    @ColorInt
    fun getCodeColor(context: Context, item: ConfigResponse): Int {
        val colorResId: Int = when (item.responseId) {
            "spn_ok" -> R.color.config_green
            "spn_item_unavailable", "spn_item_owned", "spn_item_not_owned" -> R.color.config_yellow
            "spn_dev_error", "spn_error" -> R.color.config_red
            "spn_billing_unavailable" -> R.color.config_blue
            "spn_default" -> R.color.config_gray
            else -> throw IllegalArgumentException("No color for ConfigResponse with id=" + item.responseId)
        }
        return ContextCompat.getColor(context, colorResId)
    }
}