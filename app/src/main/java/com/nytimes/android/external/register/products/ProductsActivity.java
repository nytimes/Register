package com.nytimes.android.external.register.products;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.nytimes.android.external.register.R;
import com.nytimes.android.external.register.di.Injector;
import com.nytimes.android.external.register.model.Config;
import com.nytimes.android.external.register.model.ConfigSku;
import com.nytimes.android.external.register.model.ImmutableConfigSku;
import com.nytimes.android.external.register.products.edit.EditProductActivity;
import com.nytimes.android.external.register.ui.EmptyItemDecoration;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;


public class ProductsActivity extends AppCompatActivity {

    @Inject
    protected Optional<Config> config;


    private final CompositeDisposable disposables = new CompositeDisposable();

    private View emptyView;
    private TextView emptyViewTitle;
    private TextView emptyViewText;
    private ProductsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        initToolbar();
        initRecycler();

        emptyView = findViewById(R.id.empty_view);
        emptyViewText = (TextView) findViewById(R.id.empty_view_text);
        emptyViewTitle = (TextView) findViewById(R.id.empty_view_title);
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.products);
        setSupportActionBar(toolbar);
    }

    private void initRecycler() {
        adapter = new ProductsAdapter(this);
        adapter.setHasStableIds(true);
        disposables.add(adapter.getClickSubject().subscribe(item -> {
            Intent intent = EditProductActivity.newIntent(ProductsActivity.this, item);
            startActivityForResult(intent, EditProductActivity.REQUEST_CODE);
        }));

        int padding = getResources().getDimensionPixelSize(R.dimen.padding_normal);
        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        recyclerView.setPadding(padding, padding, padding, padding);
        recyclerView.setClipToPadding(false);
        recyclerView.addItemDecoration(new EmptyItemDecoration(padding));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateProducts();
        checkEmptyState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditProductActivity.REQUEST_CODE && resultCode == RESULT_OK) {
           ConfigSku newConfigSku = ImmutableConfigSku.builder()
                    .itemType(data.getStringExtra(EditProductActivity.RESULT_CONFIG_SKU_TYPE))
                    .title(data.getStringExtra(EditProductActivity.RESULT_CONFIG_SKU_TITLE))
                    .price(data.getStringExtra(EditProductActivity.RESULT_CONFIG_SKU_PRICE))
                    .description(data.getStringExtra(EditProductActivity.RESULT_CONFIG_SKU_DESC))
                    .packageName(data.getStringExtra(EditProductActivity.RESULT_CONFIG_SKU_PACKAGE_NAME))
                    .build();
            Toast.makeText(this, "Save: " + newConfigSku.toString(), Toast.LENGTH_SHORT).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateProducts() {
        if (config.isPresent()) {
            Collection<ConfigSku> values = config.get().skus().values();
            adapter.setItems(values);
        }
    }

    private void checkEmptyState() {
        emptyView.setVisibility(!config.isPresent() || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        if (emptyView.getVisibility() == View.VISIBLE) {
            emptyViewText.setText(config.isPresent() ? R.string.empty_message_text : R.string.no_config_text);
            emptyViewTitle.setText(config.isPresent() ? R.string.empty_message_title : R.string.no_config_title);
        }
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }

}
