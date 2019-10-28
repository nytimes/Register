package com.nytimes.android.external.register.buy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nytimes.android.external.register.*
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder
import com.nytimes.android.external.register.di.Injector
import com.nytimes.android.external.register.model.Config
import com.nytimes.android.external.registerlib.GoogleUtil
import com.nytimes.android.external.registerlib.InAppPurchaseData
import java.security.InvalidKeyException
import java.security.SignatureException
import javax.inject.Inject

class BuyFragment : Fragment(), OnBackPressedListener {
    @Inject
    lateinit var apiOverrides: APIOverrides
    @Inject
    lateinit var purchases: Purchases
    @Inject
    @JvmField
    var config: Config? = null
    @Inject
    lateinit var responseHandler: ResponseHandler
    @Inject
    lateinit var purchaseHelper: PurchaseHelper
    @Inject
    lateinit var signer: Signer

    @VisibleForTesting
    internal var currentTimeMillis: Long = 0

    private lateinit var purchaseData: PurchaseData

    private val buyResponse: Int
        get() {
            var response = apiOverrides.buyResponse
            if (response == APIOverrides.RESULT_DEFAULT) {
                response = if (config == null) {
                    GoogleUtil.RESULT_ERROR
                } else if (config!!.skus[purchaseData.sku] == null) {
                    GoogleUtil.RESULT_ITEM_UNAVAILABLE
                } else if (purchases.getReceiptsForSkus(setOf(purchaseData.sku!!), purchaseData.itemtype!!).isNotEmpty()) {
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
                response = if (config == null) {
                    GoogleUtil.RESULT_ERROR
                } else if (config!!.skus[purchaseData.newSku] == null) {
                    GoogleUtil.RESULT_ITEM_UNAVAILABLE
                } else if (GoogleUtil.BILLING_TYPE_IAP == purchaseData.itemtype) {
                    GoogleUtil.RESULT_ERROR
                } else if (purchases.getReceiptsForSkus(setOf(purchaseData.newSku!!), purchaseData.itemtype!!).isNotEmpty()) {
                    GoogleUtil.RESULT_ITEM_ALREADY_OWNED
                } else if (skusNotOwned(purchaseData.oldSkus)) {
                    GoogleUtil.RESULT_ITEM_NOT_OWNED
                } else {
                    GoogleUtil.RESULT_OK
                }
            }
            return response
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.nyt_register_fragment_buy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Injector.create(requireActivity()).inject(this)
        super.onViewCreated(view, savedInstanceState)
        initPurchaseData()
    }

    private fun initPurchaseData() {
        val bundle = requireArguments()
        purchaseData = PurchaseData()
        purchaseData.sku = bundle.getString(BuyIntentBundleBuilder.EX_SKU)
        if (purchaseData.sku == null) {
            purchaseData.newSku = bundle.getString(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU)
            purchaseData.oldSkus = bundle.getStringArrayList(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS)
            if (purchaseData.oldSkus == null) {
                purchaseData.sku = purchaseData.newSku
            } else {
                purchaseData.isReplace = true
            }
        }

        purchaseData.itemtype = bundle.getString(BuyIntentBundleBuilder.EX_ITEM_TYPE)
        purchaseData.developerPayload = bundle.getString(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD)
    }

    override fun onStart() {
        super.onStart()
        currentTimeMillis = System.currentTimeMillis()
        initViews(if (purchaseData.isReplace) replaceResponse else buyResponse)
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
        val title = requireView().findViewById<View>(R.id.nyt_register_buy_title) as TextView
        title.text = if (config != null)
            content.getString(RESPONSE_EXTRA_TITLE)
        else
            getString(R.string.nyt_register_no_config_title)

        val summary = requireView().findViewById<View>(R.id.nyt_register_buy_summary) as TextView
        summary.text = if (config != null)
            content.getString(RESPONSE_EXTRA_SUMMARY)
        else
            getString(R.string.nyt_register_no_config_text)

        if (config != null && content.containsKey(RESPONSE_EXTRA_REPLACE_OLD_SKU)) {
            summary.append("\n\n" + content.getString(RESPONSE_EXTRA_REPLACE_OLD_SKU)!!)
        }
    }

    private fun initBodyResultSuccess(content: Bundle) {
        initUserSpinner()

        // init price
        val price = requireView().findViewById<View>(R.id.nyt_register_buy_price) as TextView
        if (content.containsKey(RESPONSE_EXTRA_PRICE)) {
            price.visibility = View.VISIBLE
            price.text = content.getString(RESPONSE_EXTRA_PRICE)
        } else {
            price.visibility = View.GONE
        }

        requireView().findViewById<View>(R.id.nyt_register_div).visibility = View.VISIBLE

        // init buy
        val buyButton = requireView().findViewById<View>(R.id.nyt_register_buy_button) as Button
        buyButton.setText(R.string.nyt_register_buy)
        buyButton.setOnClickListener { onBuy() }
    }

    private fun initUserSpinner() {
        val usersSpinner = requireView().findViewById<View>(R.id.nyt_register_buy_spinner_accounts) as Spinner
        usersSpinner.visibility = View.VISIBLE

        val currentUser = apiOverrides.usersResponse
        val users = if (config != null) config!!.users else listOf()
        val index = users.indexOf(currentUser)
        val selectedItem = if (index == -1) 0 else index

        val adapter = ArrayAdapter(requireActivity(),
                R.layout.nyt_register_simple_list_item, android.R.id.text1,
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
        requireView().findViewById<View>(R.id.nyt_register_buy_price).visibility = View.GONE
        requireView().findViewById<View>(R.id.nyt_register_div).visibility = View.GONE
        requireView().findViewById<View>(R.id.nyt_register_buy_spinner_accounts).visibility = View.GONE

        val buyButton = requireView().findViewById<View>(R.id.nyt_register_buy_button) as Button
        if (buyResponse == GoogleUtil.RESULT_ITEM_ALREADY_OWNED) {
            buyButton.setText(R.string.nyt_register_replace)
            buyButton.setOnClickListener { onBuyAlreadyOwned() }
        } else {
            buyButton.setText(R.string.nyt_register_ok)
            buyButton.setOnClickListener { requireActivity().finish() }
        }
    }

    private fun onBuy() {
        if (config != null) {
            val purchaseResult = purchaseHelper.onBuy(purchaseData, currentTimeMillis)
            val result = purchaseResult.successful
            val inAppPurchaseDataStr = InAppPurchaseData.toJson(purchaseResult.inAppPurchaseData)
            val resultIntent = Intent()
            resultIntent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_OK)
            resultIntent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, inAppPurchaseDataStr)
            try {
                resultIntent.putExtra(GoogleUtil.INAPP_DATA_SIGNATURE, signer.signData(inAppPurchaseDataStr))
            } catch (se: SignatureException) {
                Log.e(TAG, "Error on signature", se)
            } catch (ik: InvalidKeyException) {
                Log.e(TAG, "Invalid key", ik)
            }

            requireActivity().setResult(if (result) Activity.RESULT_OK else Activity.RESULT_CANCELED, resultIntent)
            requireActivity().finish()
        }
    }

    private fun onBuyAlreadyOwned() {
        val iterator = purchases.getInAppPurchaseData(purchaseData.itemtype!!).iterator()
        if (iterator.hasNext()) {
            val intent = Intent()
            intent.putExtra(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_ITEM_ALREADY_OWNED)
            intent.putExtra(GoogleUtil.INAPP_PURCHASE_DATA, InAppPurchaseData.toJson(iterator.next()))
            requireActivity().setResult(Activity.RESULT_OK, intent)
        } else {
            requireActivity().setResult(Activity.RESULT_CANCELED)
        }
        requireActivity().finish()
    }

    private fun getResponseContent(buyResponse: Int): Bundle {
        return responseHandler.getResponseContent(buyResponse, purchaseData)
    }

    private fun skusNotOwned(skus: List<String>?): Boolean {
        return purchases.getReceiptsForSkus(skus!!.toSet(), purchaseData.itemtype!!).size < skus.size
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionHandler.handlePermissionResult(requestCode, requireActivity(), *grantResults)
    }

    override fun backPressed() {
        val bundle = Bundle()
        bundle.putInt(GoogleUtil.RESPONSE_CODE, GoogleUtil.RESULT_USER_CANCELED)
        requireActivity().setResult(AppCompatActivity.RESULT_OK)
    }

    companion object {
        @VisibleForTesting
        const val RECEIPT_FMT = "%s.RegisterrToken%d"

        @VisibleForTesting
        const val ERROR_FMT = "%s: %d"

        @VisibleForTesting
        const val PRICE_FMT = "$%s"

        const val RESPONSE_EXTRA_TITLE = "RESPONSE_EXTRA_TITLE"
        const val RESPONSE_EXTRA_SUMMARY = "RESPONSE_EXTRA_SUMMARY"
        const val RESPONSE_EXTRA_PRICE = "RESPONSE_EXTRA_PRICE"
        const val RESPONSE_EXTRA_REPLACE_OLD_SKU = "RESPONSE_EXTRA_REPLACE_OLD_SKU"
        private const val TAG = "BuyFragment"

        fun create(
                sku: String?,
                newSku: String?,
                oldSkus: ArrayList<String?>?,
                itemType: String?,
                payload: String?
        ): BuyFragment {
            val fragment = BuyFragment()
            val args = Bundle().apply {
                putString(BuyIntentBundleBuilder.EX_SKU, sku)
                putString(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU, newSku)
                putStringArrayList(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS, oldSkus)
                putString(BuyIntentBundleBuilder.EX_ITEM_TYPE, itemType)
                putString(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD, payload)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

interface OnBackPressedListener {
    fun backPressed()
}