package com.nytimes.android.external.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nytimes.android.external.register.bundle.BuyIntentBundleBuilder
import com.nytimes.android.external.register.bundle.BuyIntentToReplaceSkusBundleBuilder
import com.nytimes.android.external.register.buy.BuyFragment

/**
 * Activity that displays dialog allowing user to purchase item
 */
open class BuyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        val fragment = BuyFragment.create(
                sku = intent.getStringExtra(BuyIntentBundleBuilder.EX_SKU),
                newSku = intent.getStringExtra(BuyIntentToReplaceSkusBundleBuilder.EX_NEW_SKU),
                oldSkus = intent.getStringArrayListExtra(BuyIntentToReplaceSkusBundleBuilder.EX_OLD_SKUS),
                itemType = intent.getStringExtra(BuyIntentBundleBuilder.EX_ITEM_TYPE),
                payload = intent.getStringExtra(BuyIntentBundleBuilder.EX_DEVELOPER_PAYLOAD)
        )

        supportFragmentManager.beginTransaction()
                .replace(R.id.buy_container, fragment)
                .commit()
    }

}
