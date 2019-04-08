package com.nytimes.android.external.register.sample

import android.content.Context
import android.content.res.ColorStateList
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.subjects.PublishSubject
import java.util.*


class SampleAdapter internal constructor(context: Context) : RecyclerView.Adapter<SampleAdapter.SampleViewHolder>() {
    private val prefsManager: PrefsManager
    private val purchasesMap: MutableMap<SkuDetails, Purchase>
    private val items: MutableList<SkuDetails>
    private val colorTesterEnbeled: ColorStateList?
    private val colorTesterDisabled: ColorStateList?

    private var inflater: LayoutInflater?
    var clickSubject: PublishSubject<SkuDetails>? = PublishSubject.create()
        private set

    init {
        inflater = LayoutInflater.from(context)
        prefsManager = PrefsManager(PreferenceManager.getDefaultSharedPreferences(context))
        purchasesMap = HashMap()
        items = ArrayList()
        colorTesterEnbeled = ContextCompat.getColorStateList(context, R.color.button_background_enabled)
        colorTesterDisabled = ContextCompat.getColorStateList(context, R.color.button_background_disabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleAdapter.SampleViewHolder {
        val v = inflater!!.inflate(R.layout.item_card, parent, false)
        return SampleViewHolder(v)
    }

    override fun onBindViewHolder(holder: SampleAdapter.SampleViewHolder, position: Int) {
        val context = holder.itemView.context

        val item = items[position]
        val isPurchased = purchasesMap.containsKey(item)

        holder.title.text = item.title
        holder.description.text = item.description
        holder.button.text = if (isPurchased) context.getString(R.string.purchased) else item.price
        holder.button.isEnabled = !isPurchased
        ViewCompat.setBackgroundTintList(holder.button, getColorTester())
        holder.button.setOnClickListener { clickSubject!!.onNext(item) }
    }

    private fun getColorTester() = if (prefsManager.isUsingTestGoogleServiceProvider)
        colorTesterEnbeled
    else
        colorTesterDisabled

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: SkuDetails) {
        this.items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun addPurchase(purchase: Purchase) {
        for (item in items) {
            if (item.sku == purchase.sku) {
                purchasesMap[item] = purchase
                notifyItemChanged(items.indexOf(item))
                break
            }
        }
    }

    fun clear() {
        purchasesMap.clear()
        items.clear()
        notifyDataSetChanged()
    }

    fun destroy() {
        if (clickSubject != null) {
            clickSubject!!.onComplete()
            clickSubject = null
        }

        inflater = null
        purchasesMap.clear()
        items.clear()
    }

    class SampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView
        val description: TextView
        val button: Button

        init {
            title = itemView.findViewById<View>(R.id.item_card_title) as TextView
            description = itemView.findViewById<View>(R.id.item_card_description) as TextView
            button = itemView.findViewById<View>(R.id.item_card_button) as Button
        }
    }
}
