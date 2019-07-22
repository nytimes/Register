package com.nytimes.android.external.register.legal

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import com.nytimes.android.external.register.R

/**
 * A basic list view to show the licenses of open
 * source projects that we use.
 */
class LegalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal)

        initToolbar()
        initRecycler()
    }

    private fun initToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initRecycler() {
        val legalRecyclerView: RecyclerView = findViewById(R.id.list)
        legalRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val inflater = LayoutInflater.from(this)
        legalRecyclerView.adapter = LegalAdapter(inflater, inflateData())
    }

    private fun inflateData(): List<Pair<String, String>> {
        val names = resources.getStringArray(R.array.license_names)
        val values = resources.getStringArray(R.array.license_values)

        return names.mapIndexed { index, name -> name to values[index] }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
