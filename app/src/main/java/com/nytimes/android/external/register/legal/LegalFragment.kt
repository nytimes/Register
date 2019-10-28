package com.nytimes.android.external.register.legal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.nytimes.android.external.register.MainActivity
import com.nytimes.android.external.register.R


class LegalFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.nyt_register_fragment_legal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecycler()
    }

    private fun initToolbar() {
        val toolbar = requireView().findViewById<Toolbar>(R.id.nyt_register_toolbar)
        toolbar.title = "Legal"
        toolbar.setNavigationIcon(R.drawable.nyt_register_ic_arrow_back)
        toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).popBackstack() }
    }

    private fun initRecycler() {
        val legalRecyclerView: RecyclerView = requireView().findViewById(R.id.nyt_register_list)
        legalRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val inflater = LayoutInflater.from(context)
        legalRecyclerView.adapter = LegalAdapter(inflater, inflateData())
    }

    private fun inflateData(): List<Pair<String, String>> {
        val names = resources.getStringArray(R.array.nyt_register_license_names)
        val values = resources.getStringArray(R.array.nyt_register_license_values)

        return names.mapIndexed { index, name -> name to values[index] }
    }

}