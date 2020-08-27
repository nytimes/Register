package com.nytimes.android.external.register.sample

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.android.material.appbar.AppBarLayout
import com.nytimes.android.external.registerlib.GoogleServiceProvider
import com.nytimes.android.external.registerlib.GoogleServiceProviderTesting
import com.nytimes.android.external.registerlib.GoogleUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.*

class SampleActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    companion object {
        const val TAG = "SampleActivity"
        const val SKU_IAP = "register.sample.iap"
        const val SKU_SUB = "register.sample.sub"
    }

    private lateinit var adapter: SampleAdapter

    private lateinit var billingApiSwitch: Switch
    private lateinit var appBarColorAnimator: ValueAnimator
    private lateinit var emptyView: View
    private lateinit var emptyTitle: TextView

    private lateinit var prefsManager: PrefsManager
    private lateinit var googleServiceProvider: GoogleServiceProvider
    private var compositeDisposable = CompositeDisposable()

    private val skuMap = mutableMapOf<String, SkuDetails>()

    private var purchasesUpdatedListener = PurchasesUpdatedListener { _, _ -> checkPurchasesAndSkuDetails() }

    private var billingClientStateListener: BillingClientStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                checkPurchasesAndSkuDetails()
            }
        }

        override fun onBillingServiceDisconnected() {
            //TODO handle disconnection case (perhaps reconnection)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        initFields()
        initToolbar()
        initEmptyView()
        initRecycler()
        initGoogleServiceProvider()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        googleServiceProvider.endConnection()
        adapter.destroy()

        super.onDestroy()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (billingApiSwitch == buttonView) {
            prefsManager.setUsingGoogleServiceProvider(isChecked)
            initGoogleServiceProvider()
            checkPurchasesAndSkuDetails()

            if (isChecked) {
                appBarColorAnimator.start()
            } else {
                appBarColorAnimator.reverse()
            }
        }
    }

    fun checkPurchasesAndSkuDetails() {
        if (googleServiceProvider.isReady()) {
            val subSkuDetailsStream = getSubSkuDetailsStream()

            val purchasesResultStream = Observable.fromCallable<List<Purchase>> {
                val types = listOf(
                        GoogleUtil.BILLING_TYPE_IAP, GoogleUtil.BILLING_TYPE_SUBSCRIPTION)
                val purchases = ArrayList<Purchase>()
                for (type in types) {
                    val result = googleServiceProvider.queryPurchases(type)
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchases.addAll(result.purchasesList as List<Purchase>)
                    }
                }
                purchases
            }

            getSkuDetails(getIapSkuDetailsStream(subSkuDetailsStream), purchasesResultStream)
        }
    }

    private fun getSubSkuDetailsStream(): Observable<List<SkuDetails>> {
        val subParams = SkuDetailsParams
                .newBuilder()
                .setSkusList(listOf(SKU_SUB))
                .setType(BillingClient.SkuType.SUBS)
                .build()

        return Observable.create { emitter ->
            googleServiceProvider.querySkuDetailsAsync(subParams, SkuDetailsResponseListener { _, skuDetailsList ->
                if (skuDetailsList == null) {
                    emitter.onNext(ArrayList())
                } else {
                    skuDetailsList.forEach {
                        skuMap[it.sku] = it
                    }
                    emitter.onNext(skuDetailsList)
                }
                emitter.onComplete()
            })
        }
    }

    private fun getIapSkuDetailsStream(
            subSkuDetailsStream: Observable<List<SkuDetails>>
    ): Observable<List<SkuDetails>> {
        val iapParams = SkuDetailsParams
                .newBuilder()
                .setSkusList(listOf(SKU_IAP))
                .setType(BillingClient.SkuType.INAPP)
                .build()

        val iapSkuDetailsStream = Observable.create<List<SkuDetails>> { emitter ->
            googleServiceProvider.querySkuDetailsAsync(iapParams, SkuDetailsResponseListener { _, skuDetailsList ->
                if (skuDetailsList == null) {
                    emitter.onNext(ArrayList())
                } else {
                    skuDetailsList.forEach {
                        skuMap[it.sku] = it
                    }
                    emitter.onNext(skuDetailsList)
                }
                emitter.onComplete()
            })
        }

        return iapSkuDetailsStream.flatMap { iapSkuDetails ->
            subSkuDetailsStream.map<List<SkuDetails>> { subsSkuDetails ->
                val combined = ArrayList<SkuDetails>()
                combined.addAll(iapSkuDetails)
                combined.addAll(subsSkuDetails)
                combined
            }
        }
    }

    private fun getSkuDetails(
            iapSkuDetailsStream: Observable<List<SkuDetails>>,
            purchasesResultStream: Observable<List<Purchase>>?
    ) {
        compositeDisposable.add(
                Observable.combineLatest<List<SkuDetails>, List<Purchase>, Pair<List<SkuDetails>, List<Purchase>>>(
                        iapSkuDetailsStream,
                        purchasesResultStream,
                        BiFunction<List<SkuDetails>, List<Purchase>, Pair<List<SkuDetails>, List<Purchase>>> { first, second -> Pair(first, second) }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    adapter.clear()
                                    result.first.forEach { adapter.addItem(it) }
                                    result.second.forEach { adapter.addPurchase(it) }
                                    checkEmptyState()
                                },
                                { throwable -> Log.e(TAG, "Failed to retrieve products and purchases", throwable) }
                        )
        )
    }

    private fun initFields() {
        prefsManager = PrefsManager(PreferenceManager.getDefaultSharedPreferences(this))
        compositeDisposable = CompositeDisposable()
    }

    private fun initToolbar() {
        val isUsingTestProvider = prefsManager.isUsingTestGoogleServiceProvider

        val appBarEnabled = ContextCompat.getColor(this, R.color.colorPrimary)
        val appBarDisabled = ContextCompat.getColor(this, R.color.colorPrimaryAlt)

        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar_layout)
        appBarLayout.setOnClickListener { billingApiSwitch.toggle() }
        appBarLayout.setBackgroundColor(if (isUsingTestProvider) appBarEnabled else appBarDisabled)
        appBarLayout.setPadding(appBarLayout.paddingLeft,
                getStatusBarHeight(),
                appBarLayout.paddingRight,
                appBarLayout.paddingBottom)

        appBarColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), appBarDisabled, appBarEnabled)
        appBarColorAnimator.currentPlayTime = if (isUsingTestProvider)
            appBarColorAnimator.duration
        else
            0
        appBarColorAnimator.addUpdateListener { animator -> appBarLayout.setBackgroundColor(animator.animatedValue as Int) }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        billingApiSwitch = findViewById(R.id.app_bar_switch)
        billingApiSwitch.isChecked = isUsingTestProvider
        billingApiSwitch.setOnCheckedChangeListener(this)
    }

    private fun initEmptyView() {
        emptyView = findViewById(R.id.empty_view)
        emptyView.visibility = View.GONE

        emptyTitle = findViewById(R.id.empty_message)
        emptyTitle.setText(if (prefsManager.isUsingTestGoogleServiceProvider)
            R.string.empty_message_register
        else
            R.string.empty_message_google)
    }

    private fun initRecycler() {
        adapter = SampleAdapter(this)
        compositeDisposable.add(adapter.clickSubject!!.subscribe { item ->
            skuMap[item.sku]?.let { skuDetails ->
                val params = BillingFlowParams
                        .newBuilder()
                        .setSkuDetails(skuDetails)
                        .build()
                googleServiceProvider.launchBillingFlow(this, params)
            }
        })

        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        val dividerSize = resources.getDimensionPixelSize(R.dimen.recycler_divider_size)
        recyclerView.addItemDecoration(EmptyItemDecoration(dividerSize))
    }

    private fun initGoogleServiceProvider() {
        googleServiceProvider = GoogleServiceProviderTesting
                .newBuilder(this)
                .useTestProvider(prefsManager.isUsingTestGoogleServiceProvider)
                .setListener(purchasesUpdatedListener)
               // .enablePendingPurchases() // Not used for subscriptions
                .build()

        googleServiceProvider.startConnection(billingClientStateListener)
    }

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            emptyView.visibility = View.VISIBLE
            emptyTitle.setText(if (prefsManager.isUsingTestGoogleServiceProvider)
                R.string.empty_message_register
            else
                R.string.empty_message_google)
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            resources.getDimensionPixelSize(R.dimen.status_bar_height)
        }
    }

}
