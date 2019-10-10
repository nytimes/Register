package com.nytimes.android.external.register.legal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nytimes.android.external.register.R

/**
 * A basic list view to show the licenses of open
 * source projects that we use.
 */
class LegalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal)

        supportFragmentManager.beginTransaction()
                .replace(R.id.legal_fragment_container, LegalFragment())
                .commit()
    }
}
