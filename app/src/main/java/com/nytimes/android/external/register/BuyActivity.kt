package com.nytimes.android.external.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.Optional
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder
import com.nytimes.android.external.register.di.Injector
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.InAppPurchaseData
import java.security.InvalidKeyException
import java.security.SignatureException
import java.util.*
import javax.inject.Inject

/**
 * Activity that displays dialog allowing user to purchase item
 */
open class BuyActivity : AppCompatActivity() {

    @Inject
    lateinit var apiOverrides: APIOverrides
    @Inject
    lateinit var purchases: Purchases
    @Inject
    lateinit var config: Optional<Config>
    @Inject
    lateinit var signer: Signer

    @VisibleForTesting
    internal var currentTimeMillis: Long = 0

    private var sku: String? = null
    private var newSku: String? = null
    private var oldSkus: List<String>? = null
    private var itemtype: String? = null
    private var developerPayload: String? = null
    private var isReplace = false

    private val buyResponse: Int
        get() {
            var response = apiOverrides.buyResponse
            if (response == APIOverrides.RESULT_DEFAULT) {
                response = if (!config.isPresent) {
                    GoogleUtil.RESULT_ERROR
                } else if (config.get().skus[sku] == null) {
                    GoogleUtil.RESULT_ITEM_UNAVAILABLE
                } else if (purchases.getReceiptsForSkus(ImmutableSet.of(sku!!), itemtype!!).isNotEmpty()) {
                    GoogleUtil.RESULT_ITEM_ALREADY_OWNED
                } else {
                    GoogleUtil.RESULT_OK
                }
            }
            return response
        }

    private val replaceResponse: Int
        get() {
            var response = apiOverrides.replaceResponse
            if (response == APIOverrides.RESULT_DEFAULT) {
                response = if (!config.isPresent) {
                    GoogleUtil.RESULT_ERROR
                } else if (config.get().skus[newSku] == null) {
                    GoogleUtil.RESULT_ITEM_UNAVAILABLE
                } else if (GoogleUtil.BILLING_TYPE_IAP == itemtype) {
                    GoogleUtil.RESULT_ERROR
                } else if (purchases.getReceiptsForSkus(ImmutableSet.of(newSku!!), itemtype!!).isNotEmpty()) {
                    GoogleUtil.RESULT_ITEM_ALREADY_OWNED
                } else if (skusNotOwned(oldSkus)) {
                    GoogleUtil.RESULT_ITEM_NOT_OWNED
                } else {
                    GoogleUtil.RESULT_OK
                }
            }
            return response
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        initPurchaseData()
    }

    protected open fun inject() {
        Injector.create(this).inject(this)
    }

    private fun initPurchaseData() {
        sku = intent.getStringExtra(BuyIntentBundleBuilder.EX_SKU)
        if (sku == null) {
            newSku = intent.getStringExtra(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU)
            oldSkus = intent.getStringArrayListExtra(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS)
            if (oldSkus == null) {
                sku = newSku
            } else {
                isReplace = true
            }
        }

        itemtype = intent.getStringExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE)
        developerPayload = intent.getStringExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD)
    }

    override fun onStart() {
        super.onStart()
        currentTimeMillis = System.currentTimeMillis()
        initViews(if (isReplace) replaceResponse else buyResponse)
    }

    private fun initViews(buyResponse: Int) {
        val content = getResponseContent(buyResponse)
        initTextBody(content)
        if (buyResponse == GoogleUtil.RESULT_OK) {
            initBodyResultSuccess(content)
        } else {
            initBodyResultFail(buyResponse)
        }
    }

    private fun initTextBody(content: Bundle) {
        val title = findViewById<View>(R.id.buy_title) as TextView
        title.text = if (config.isPresent)
            content.getString(RESPONSE_EXTRA_TITLE)
        else
            getString(R.string.no_config_title)

        val summary = findViewById<View>(R.id.buy_summary) as TextView
        summary.text = if (config.isPresent)
            content.getString(RESPONSE_EXTRA_SUMMARY)
        else
            getString(R.string.no_config_text)

        if (config.isPresent && content.containsKey(RESPONSE_EXTRA_REPLACE_OLD_SKU)) {
            summary.append("\n\n" + content.getString(RESPONSE_EXTRA_REPLACE_OLD_SKU)!!)
        }
    }

    private fun initBodyResultSuccess(content: Bundle) {
        initUserSpinner()

        // init price
        val price = findViewById<View>(R.id.buy_price) as TextView
        if (content.containsKey(RESPONSE_EXTRA_PRICE)) {
            price.visibility = View.VISIBLE
            price.text = content.getString(RESPONSE_EXTRA_PRICE)
        } else {
            price.visibility = View.GONE
        }

        findViewById<View>(R.id.div).visibility = View.VISIBLE

        // init buy
        val buyButton = findViewById<View>(R.id.buy_button) as Button
        buyButton.setText(R.string.buy)
        buyButton.setOnClickListener { onBuy() }
    }

    private fun initUserSpinner() {
        val usersSpinner = findViewById<View>(R.id.buy_spinner_accounts) as Spinner
        usersSpinner.visibility = View.VISIBLE

        val currentUser = apiOverrides.usersResponse
        val users = if (config.isPresent) config.get().users else ImmutableList.of()
        val index = users.indexOf(currentUser)
        val selectedItem = if (index == -1) 0 else index

        val adapter = ArrayAdapter(this,
                R.layout.simple_list_item, android.R.id.text1,
                users)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        usersSpinner.adapter = adapter
        usersSpinner.setSelection(selectedItem, false)
        usersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                apiOverrides.setUsersReponse(users[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No op
            }
        }
    }

    private fun initBodyResultFail(buyResponse: Int) {
        findViewById<View>(R.id.buy_price).visibility = View.GONE
        findViewById<View>(R.id.div).visibility = View.GONE
        findViewById<View>(R.id.buy_spinner_accounts).visibility = View.GONE

        val buyButton = findViewById<View>(R.id.buy_button) as Button
        if (buyResponse == GoogleUtil.RESULT_ITEM_ALREADY_OWNED) {
            buyButton.setText(R.string.replace)
            buyButton.setOnClickListener { onBuyAlreadyOwned() }
        } else {
            buyButton.setText(R.string.ok)
            buyButton.setOnClickListener { finish() }
        }
    }

    private fun onBuy() {
        if (config.isPresent) {
            val newReceipt = String.format(Locale.getDefault(), RECEIPT_FMT,
                    apiOverrides.usersResponse, currentTimeMillis)
            val resultIntent = Intent()
            resultIntent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_OK)
            val skuToPurchase = if (isReplace) newSku else sku
            val inAppPurchaseData = InAppPurchaseData.Builder()
                    .orderId(java.lang.Long.toString(currentTimeMillis))
                    .packageName(config.get().skus[skuToPurchase]!!.packageName)
                    .productId(skuToPurchase)
                    .purchaseTime(java.lang.Long.toString(currentTimeMillis))
                    .developerPayload(developerPayload)
                    .purchaseToken(newReceipt)
                    .purchaseState("0")
                    .build()
            val inAppPurchaseDataStr = InAppPurchaseData.toJson(inAppPurchaseData)
            val result: Boolean = if (isReplace) {
                purchases.replacePurchase(inAppPurchaseDataStr, oldSkus!!)
            } else {
                purchases.addPurchase(inAppPurchaseDataStr, itemtype!!)
            }
            resultIntent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, inAppPurchaseDataStr)
            try {
                resultIntent.putExtra(GoogleUtil.INAPP_DATA_SIGNATURE, signer.signData(inAppPurchaseDataStr))
            } catch (se: SignatureException) {
                Log.e(TAG, "Error on signature", se)
            } catch (ik: InvalidKeyException) {
                Log.e(TAG, "Invalid key", ik)
            }

            setResult(if (result) Activity.RESULT_OK else Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun onBuyAlreadyOwned() {
        val iterator = purchases.getInAppPurchaseData(itemtype!!).iterator()
        if (iterator.hasNext()) {
            val intent = Intent()
            intent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
            intent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, InAppPurchaseData.toJson(iterator.next()))
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private fun getResponseContent(buyResponse: Int): Bundle {
        val bundle = Bundle()
        when (buyResponse) {
            GoogleUtil.RESULT_OK -> if (config.isPresent) {
                val configSku = config.get().skus[if (isReplace) newSku else sku]!!

                bundle.putString(RESPONSE_EXTRA_TITLE, configSku.title)
                bundle.putString(RESPONSE_EXTRA_SUMMARY, configSku.description)
                bundle.putString(RESPONSE_EXTRA_PRICE, String.format(PRICE_FMT, configSku.price))

                if (isReplace) {
                    val oldSkuTitles = ArrayList<String>()
                    for (oldSku in oldSkus!!) {
                        oldSkuTitles.add(config.get().skus[oldSku]!!.title + "\n")
                    }
                    bundle.putStringArrayList(RESPONSE_EXTRA_REPLACE_OLD_SKU, oldSkuTitles)
                }
            }
            GoogleUtil.RESULT_ITEM_UNAVAILABLE -> {
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error))
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.item_not_found))
            }
            GoogleUtil.RESULT_ITEM_ALREADY_OWNED -> {
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error))
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.item_already_owned))
            }
            GoogleUtil.RESULT_ITEM_NOT_OWNED -> {
                bundle.putString(RESPONSE_EXTRA_TITLE, getString(R.string.error))
                bundle.putString(RESPONSE_EXTRA_SUMMARY, getString(R.string.replace_item_not_owned))
            }
            GoogleUtil.RESULT_DEVELOPER_ERROR, GoogleUtil.RESULT_ERROR -> {
                val title = getString(R.string.error)
                bundle.putString(RESPONSE_EXTRA_TITLE, title)
                bundle.putString(RESPONSE_EXTRA_SUMMARY,
                        String.format(Locale.getDefault(), ERROR_FMT, title, buyResponse))
            }
            else -> {
                val title = getString(R.string.error)
                bundle.putString(RESPONSE_EXTRA_TITLE, title)
                bundle.putString(RESPONSE_EXTRA_SUMMARY, String.format(Locale.getDefault(), ERROR_FMT, title, buyResponse))
            }
        }
        return bundle
    }

    private fun skusNotOwned(skus: List<String>?): Boolean {
        return purchases.getReceiptsForSkus(ImmutableSet.copyOf(skus!!), itemtype!!).size < skus.size
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isFinishing) {
            val bundle = Bundle()
            bundle.putInt(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_USER_CANCELED)
            setResult(RESULT_OK)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionHandler.handlePermissionResult(requestCode, this, *grantResults)
    }

    companion object {
        @VisibleForTesting
        const val RECEIPT_FMT = "%s.RegisterrToken%d"

        @VisibleForTesting
        const val ERROR_FMT = "%s: %d"

        @VisibleForTesting
        const val PRICE_FMT = "$%s"

        private const val RESPONSE_EXTRA_TITLE = "RESPONSE_EXTRA_TITLE"
        private const val RESPONSE_EXTRA_SUMMARY = "RESPONSE_EXTRA_SUMMARY"
        private const val RESPONSE_EXTRA_PRICE = "RESPONSE_EXTRA_PRICE"
        private const val RESPONSE_EXTRA_REPLACE_OLD_SKU = "RESPONSE_EXTRA_REPLACE_OLD_SKU"
        private const val TAG = "BuyActivity"
    }
}
