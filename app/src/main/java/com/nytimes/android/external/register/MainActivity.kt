package com.nytimes.android.external.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Controller app for Play Billing Tester Service
 * Allows user to
 * * Start/stop service
 * * Display/Purge purchased items
 */
open class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, MainFragment())
                .commit()
    }
}
