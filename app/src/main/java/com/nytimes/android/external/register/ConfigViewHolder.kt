package com.nytimes.android.external.register

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

internal class ConfigViewHolder(val itemView: View) {

    private val codeView: TextView = itemView.findViewById(R.id.nyt_register_item_config_code)
    private val nameView: TextView = itemView.findViewById(R.id.nyt_register_item_config_name)
    private val iconView: ImageView = itemView.findViewById(R.id.nyt_register_item_config_down_icon)

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
            "nyt_register_spn_ok" -> R.color.nyt_register_config_green
            "nyt_register_spn_item_unavailable", "nyt_register_spn_item_owned", "nyt_register_spn_item_not_owned" -> R.color.nyt_register_config_yellow
            "nyt_register_spn_dev_error", "nyt_register_spn_error" -> R.color.nyt_register_config_red
            "nyt_register_spn_billing_unavailable" -> R.color.nyt_register_config_blue
            "nyt_register_spn_default" -> R.color.nyt_register_config_gray
            else -> throw IllegalArgumentException("No color for ConfigResponse with id=" + item.responseId)
        }
        return ContextCompat.getColor(context, colorResId)
    }
}