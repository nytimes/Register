package com.nytimes.android.external.playbillingtester.legal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.nytimes.android.external.playbillingtester.R;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A basic list view to show the licenses of open
 * source projects that we use.
 */
public class LegalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_legal);
        initToolbar();
        initRecycler();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRecycler() {
        RecyclerView legalRecyclerView = findViewById(R.id.list);
        legalRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        List<Map.Entry<String, String>> items = inflateData();
        LegalAdapter legalAdapter = new LegalAdapter(this, items);
        legalRecyclerView.setAdapter(legalAdapter);
    }

    @NonNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<Map.Entry<String, String>> inflateData() {
        String[] names = getResources().getStringArray(R.array.license_names);
        String[] values = getResources().getStringArray(R.array.license_values);

        List<Map.Entry<String, String>> licenseList = new ArrayList<>(names.length);
        for (int index = 0; index < names.length; index++) {
            licenseList.add(new AbstractMap.SimpleEntry<>(names[index], values[index]));
        }
        return licenseList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
