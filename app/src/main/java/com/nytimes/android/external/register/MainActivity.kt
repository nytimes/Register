package com.nytimes.android.external.register

import android.content.Intent
import android.os.Bundle
import android.support.annotation.ArrayRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.common.base.Optional
import com.nytimes.android.external.register.di.Injector
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.InAppPurchaseData
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Display/Purge purchased items
 */
open class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var purchases: Purchases
    @Inject
    lateinit var apiDelegate: APIOverridesDelegate
    @Inject
    lateinit var config: Optional<Config>

    private val disposables = CompositeDisposable()

    private lateinit var mainAdapter: MainAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var emptyViewTitle: TextView
    private lateinit var emptyViewText: TextView

    private var configureMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolbar()
        initAppBarLayout()
        initRecycler()

        emptyView = findViewById(R.id.empty_view)
        emptyViewText = findViewById<View>(R.id.empty_view_text) as TextView
        emptyViewTitle = findViewById<View>(R.id.empty_view_title) as TextView
    }

    protected open fun inject() {
        Injector.create(this).inject(this)
    }

    private fun initToolbar() {
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar).also {
            setTitle(R.string.app_name)
        })
    }

    private fun initAppBarLayout() {
        val appBarContent = findViewById<View>(R.id.app_bar_content)
        appBarLayout = findViewById(R.id.app_bar_layout)
        appBarLayout.setExpanded(false, false)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            appBarContent.translationY = (-verticalOffset).toFloat()

            val verticalOffsetAbs = Math.abs(verticalOffset).toFloat()
            val totalScrollRange = appBarLayout.totalScrollRange.toFloat()
            val normalizedOffset = verticalOffsetAbs / totalScrollRange

            if (configureMenuItem != null) {
                configureMenuItem?.icon?.alpha = (normalizedOffset * 255).toInt()
                configureMenuItem?.isVisible = normalizedOffset > 0
            }

            val inverseVerticalOffset = totalScrollRange + verticalOffset
            val startCenter = emptyView.height / 2f
            val endCenter = (emptyView.height - inverseVerticalOffset) / 2
            val emptyYOffset = startCenter - endCenter
            emptyView.translationY = -emptyYOffset
        })
    }

    private fun initRecycler() {
        swipeRefresh = findViewById(R.id.swiperefresh)
        swipeRefresh.setOnRefreshListener(this)

        mainAdapter = MainAdapter(this).apply {
            disposables.add(deleteSubject.subscribe { pair ->
                val inAppPurchaseDataStr = InAppPurchaseData.toJson(pair.second)
                if (purchases.removePurchase(inAppPurchaseDataStr, pair.first)) {
                    remove(pair)
                    checkEmptyState()
                }
            })
        }

        findViewById<RecyclerView>(R.id.list).apply {
            setHasFixedSize(true)
            adapter = mainAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        initSpinner(R.id.isBillingSupported, R.string.is_billing_supported, R.array.isBillingEnabled_spinner)
        initSpinner(R.id.getBuyIntent, R.string.get_buy_intent, R.array.getBuyIntent_spinner)
        initSpinner(R.id.buy, R.string.buy_label, R.array.buy_spinner)
        initSpinner(R.id.getPurchases, R.string.get_purchases, R.array.getPurchases_spinner)
        initSpinner(R.id.getSkuDetails, R.string.get_sku_details, R.array.getSkuDetails_spinner)
        initSpinner(R.id.consumePurchase, R.string.consume_purchases, R.array.consumePurchase_spinner)
        initSpinner(R.id.getBuyIntentToReplaceSkus, R.string.buy_intent_replace_skus,
                R.array.buy_intent_replace_skus_spinner)
    }

    private fun initSpinner(@IdRes containerLayoutId: Int, @StringRes titleResId: Int, @ArrayRes entriesResId: Int) {
        val container = findViewById<View>(containerLayoutId)

        // Set title
        container.findViewById<TextView>(R.id.config_title).apply {
            setText(titleResId)
        }

        // Init Spinner
        container.findViewById<Spinner>(R.id.config_spinner).apply {
            // Get data
            val items = getData(entriesResId)

            tag = "IGNORE_FIRST_SELECT"
            adapter = ConfigSpinnerAdapter(context, items)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (tag == null) {
                        val item = adapter.getItem(position) as ConfigResponse
                        apiDelegate.setApiOverridesValue(containerLayoutId, item)
                    } else {
                        tag = null
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // No op
                }
            }

            val currentConfigResponseCode = apiDelegate.getApiOverridesValue(containerLayoutId)
            val spinnerSelectionIndex = getResponseCodeListIndex(currentConfigResponseCode, items)
            if (spinnerSelectionIndex != -1) {
                setSelection(spinnerSelectionIndex, false)
            }
        }
    }

    private fun getData(@ArrayRes entriesResId: Int): List<ConfigResponse> {
        val items = ArrayList<ConfigResponse>()
        val entries = resources.getStringArray(entriesResId)
        for (id in entries) {
            items.add(ConfigResponse(id, getConfigResponseName(id), getConfigResponseCode(id)))
        }
        return items
    }

    private fun getConfigResponseName(name: String): String {
        val resId = resources.getIdentifier(name, "string", packageName)
        return resources.getString(resId)
    }

    private fun getConfigResponseCode(name: String): Int {
        val resourceCodeName = name + "_code"
        val resId = resources.getIdentifier(resourceCodeName, "integer", packageName)
        return resources.getInteger(resId)
    }

    private fun getResponseCodeListIndex(responseCode: Int, items: List<ConfigResponse>): Int {
        var index = 0
        var i = 0
        val size = items.size
        while (i < size) {
            val (_, _, responseCode1) = items[i]
            if (responseCode1 == responseCode) {
                index = i
                break
            }
            i++
        }
        return index
    }

    override fun onPostResume() {
        super.onPostResume()
        updatePurchases()
        checkEmptyState()
    }

    private fun updatePurchases() {
        if (config.isPresent) {
            val items = ArrayList<Pair<String, InAppPurchaseData>>()
            for (sub in purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_SUBSCRIPTION)) {
                items.add(Pair.create(GoogleUtil.BILLING_TYPE_SUBSCRIPTION, sub))
            }
            for (iap in purchases.getInAppPurchaseData(GoogleUtil.BILLING_TYPE_IAP)) {
                items.add(Pair.create(GoogleUtil.BILLING_TYPE_IAP, iap))
            }
            items.sortWith(Comparator { l, r ->
                val purchaseTimeL = java.lang.Long.parseLong(l.second.purchaseTime())
                val purchaseTimeR = java.lang.Long.parseLong(r.second.purchaseTime())
                java.lang.Long.compare(purchaseTimeR, purchaseTimeL)
            })
            mainAdapter.items = items
        }
    }

    private fun checkEmptyState() {
        emptyView.visibility = if (!config.isPresent || mainAdapter.itemCount == 0) View.VISIBLE else View.GONE
        if (emptyView.visibility == View.VISIBLE) {
            emptyViewText.setText(if (config.isPresent) R.string.empty_message_text else R.string.no_config_text)
            emptyViewTitle.setText(if (config.isPresent) R.string.empty_message_title else R.string.no_config_title)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        configureMenuItem = menu.findItem(R.id.menu_action_configure)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_action_delete_all -> {
                purchases.purgePurchases()
                swipeRefresh.isRefreshing = true
                swipeRefresh.postDelayed({ this.onRefresh() }, 300)
                return true
            }
            R.id.menu_action_refresh -> {
                swipeRefresh.isRefreshing = true
                swipeRefresh.postDelayed({ this.onRefresh() }, 300)
                return true
            }
            R.id.menu_action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                return true
            }
            R.id.menu_action_configure -> {
                appBarLayout.setExpanded(true, true)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionHandler.handlePermissionResult(requestCode, this, *grantResults)
    }

    override fun onRefresh() {
        swipeRefresh.postDelayed({ swipeRefresh.isRefreshing = false }, 300)
        updatePurchases()
        checkEmptyState()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
