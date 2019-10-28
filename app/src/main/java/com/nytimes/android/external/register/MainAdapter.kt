package com.nytimes.android.external.register

import android.content.Context
import android.text.format.DateUtils
import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nytimes.android.external.registerlib.InAppPurchaseData
import io.reactivex.subjects.PublishSubject

class MainAdapter(context: Context) : RecyclerView.Adapter<MainViewHolder>() {

    val deleteSubject: PublishSubject<Pair<String, InAppPurchaseData>> = PublishSubject.create()
    var items: MutableList<Pair<String, InAppPurchaseData>> = ArrayList()
        set(value) {
            field = ArrayList(value)
            notifyDataSetChanged()
        }
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return with(inflater.inflate(R.layout.nyt_register_item_main, parent, false)) {
            MainViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        with(items[position]) {
            holder.productTitle.text = second.purchaseToken()
            holder.productTypeView.text = second.productId()
            holder.purchaseDateView.text = getTimeLapsed(second.purchaseTime())
            holder.deletePurchase.setOnClickListener { v -> deleteSubject.onNext(this) }
        }
    }

    private fun getTimeLapsed(timeString: String?): CharSequence {
        val purchaseTime = java.lang.Long.parseLong(timeString)
        return DateUtils.getRelativeTimeSpanString(purchaseTime,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun remove(pair: Pair<String, InAppPurchaseData>) {
        val index = items.indexOf(pair)
        if (index != -1 && items.remove(pair)) {
            notifyItemRemoved(index)
        }
    }

}
