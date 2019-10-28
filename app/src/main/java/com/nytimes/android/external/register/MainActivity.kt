package com.nytimes.android.external.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder
import com.nytimes.android.external.register.buy.BuyFragment
import com.nytimes.android.external.register.buy.OnBackPressedListener
import com.nytimes.android.external.register.legal.LegalFragment

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Display/Purge purchased items
 */
open class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BUY_FLOW = "EXTRA_BUY_FLOW"
    }

    private var backPressedListener: OnBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nyt_register_activity_main)

        if (intent.hasExtra(EXTRA_BUY_FLOW)) {
            navigateToBuy()
        } else if (savedInstanceState == null) {
            navigateTo(MainFragment(), addToBackStack = false)
        }
    }

    fun navigateToSettings() {
        navigateTo(SettingsFragment())
    }

    fun navigateToLegal() {
        navigateTo(LegalFragment())
    }

    private fun navigateToBuy() {
        val fragment = BuyFragment.create(
                sku = intent.getStringExtra(BuyIntentBundleBuilder.EX_SKU),
                newSku = intent.getStringExtra(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU),
                oldSkus = intent.getStringArrayListExtra(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS),
                itemType = intent.getStringExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE),
                payload = intent.getStringExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD)
        )
        backPressedListener = fragment
        navigateTo(fragment, addToBackStack = false)
    }

    fun popBackstack() {
        supportFragmentManager.popBackStack()
    }

    private fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        fragment.retainInstance = true

        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.nyt_register_main_fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isFinishing) {
            backPressedListener?.backPressed()
        }
    }
}
