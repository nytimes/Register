package com.nytimes.android.external.register.products.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nytimes.android.external.register.R;
import com.nytimes.android.external.register.di.Injector;
import com.nytimes.android.external.register.model.ConfigSku;


public class EditProductActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1000;

    private static final String EXTRA_CONFIG_SKU_TYPE = "EditProductActivity.EXTRA_CONFIG_SKU_TYPE";
    private static final String EXTRA_CONFIG_SKU_TITLE = "EditProductActivity.EXTRA_CONFIG_SKU_TITLE";
    private static final String EXTRA_CONFIG_SKU_PRICE = "EditProductActivity.EXTRA_CONFIG_SKU_PRICE";
    private static final String EXTRA_CONFIG_SKU_DESC = "EditProductActivity.EXTRA_CONFIG_SKU_DESC";
    private static final String EXTRA_CONFIG_SKU_PACKAGE_NAME = "EditProductActivity.EXTRA_CONFIG_SKU_PACKAGE_NAME";

    public static final String RESULT_CONFIG_SKU_TYPE = "EditProductActivity.Result.EXTRA_CONFIG_SKU_TYPE";
    public static final String RESULT_CONFIG_SKU_TITLE = "EditProductActivity.Result.EXTRA_CONFIG_SKU_TITLE";
    public static final String RESULT_CONFIG_SKU_PRICE = "EditProductActivity.Result.EXTRA_CONFIG_SKU_PRICE";
    public static final String RESULT_CONFIG_SKU_DESC = "EditProductActivity.Result.EXTRA_CONFIG_SKU_DESC";
    public static final String RESULT_CONFIG_SKU_PACKAGE_NAME = "EditProductActivity.Result.EXTRA_CONFIG_SKU_PACKAGE_NAME";

    public static Intent newIntent(Context context) {
        return new Intent(context, EditProductActivity.class);
    }

    public static Intent newIntent(Context context, ConfigSku configSku) {
        Intent intent = newIntent(context);
        intent.putExtra(EXTRA_CONFIG_SKU_TYPE, configSku.itemType());
        intent.putExtra(EXTRA_CONFIG_SKU_TITLE, configSku.title());
        intent.putExtra(EXTRA_CONFIG_SKU_PRICE, configSku.price());
        intent.putExtra(EXTRA_CONFIG_SKU_DESC, configSku.description());
        intent.putExtra(EXTRA_CONFIG_SKU_PACKAGE_NAME, configSku.packageName());
        return intent;
    }

    private EditText type;
    private EditText title;
    private EditText price;
    private EditText desc;
    private EditText packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_edit_product);

        initToolbar();
        initBody();
        initFab();
    }

    protected void inject() {
        Injector.create(this).inject(this);
    }

    private void initToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().hasExtra(EXTRA_CONFIG_SKU_PACKAGE_NAME) ?
                    R.string.edit_product : R.string.new_product);
        }
    }

    private void initBody() {
        View.OnFocusChangeListener onFocusChangeListener = (view, hasFocus) -> {
            if (hasFocus && view instanceof EditText) {
                ((EditText) view).setError(null);
            }
        };

        type = findViewById(R.id.edit_type);
        type.setOnFocusChangeListener(onFocusChangeListener);
        if (getIntent().hasExtra(EXTRA_CONFIG_SKU_TYPE)) {
            type.setText(getIntent().getStringExtra(EXTRA_CONFIG_SKU_TYPE));
        }

        title = findViewById(R.id.edit_title);
        title.setOnFocusChangeListener(onFocusChangeListener);
        if (getIntent().hasExtra(EXTRA_CONFIG_SKU_TITLE)) {
            title.setText(getIntent().getStringExtra(EXTRA_CONFIG_SKU_TITLE));
        }

        price = findViewById(R.id.edit_price);
        price.setOnFocusChangeListener(onFocusChangeListener);
        if (getIntent().hasExtra(EXTRA_CONFIG_SKU_PRICE)) {
            price.setText(getIntent().getStringExtra(EXTRA_CONFIG_SKU_PRICE));
        }

        desc = findViewById(R.id.edit_desc);
        desc.setOnFocusChangeListener(onFocusChangeListener);
        if (getIntent().hasExtra(EXTRA_CONFIG_SKU_DESC)) {
            desc.setText(getIntent().getStringExtra(EXTRA_CONFIG_SKU_DESC));
        }

        packageName = findViewById(R.id.edit_package);
        packageName.setOnFocusChangeListener(onFocusChangeListener);
        if (getIntent().hasExtra(EXTRA_CONFIG_SKU_PACKAGE_NAME)) {
            packageName.setText(getIntent().getStringExtra(EXTRA_CONFIG_SKU_PACKAGE_NAME));
        }
    }

    //TODO Validation code
    private void initFab() {
        findViewById(R.id.edit_save).setOnClickListener(view -> {
            if (validateField(title) &&
                    validateField(price) &&
                    validateField(desc) &&
                    validateField(type) &&
                    validateField(packageName)) {
                Intent result = new Intent();
                result.putExtra(RESULT_CONFIG_SKU_TYPE, type.getText().toString());
                result.putExtra(RESULT_CONFIG_SKU_TITLE, title.getText().toString());
                result.putExtra(RESULT_CONFIG_SKU_PRICE, price.getText().toString());
                result.putExtra(RESULT_CONFIG_SKU_DESC, desc.getText().toString());
                result.putExtra(RESULT_CONFIG_SKU_PACKAGE_NAME, packageName.getText().toString());
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    private boolean validateField(EditText type) {
        if (TextUtils.isEmpty(type.getText())) {
            type.requestFocus();
            type.setError("Invalid " + type.getHint());
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
