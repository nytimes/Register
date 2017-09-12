package com.nytimes.android.external.register.di;

import com.nytimes.android.external.register.BuyActivity;
import com.nytimes.android.external.register.MainActivity;
import com.nytimes.android.external.register.SettingsActivity;
import com.nytimes.android.external.register.products.ProductsActivity;
import com.nytimes.android.external.register.products.edit.EditProductActivity;

import dagger.Subcomponent;

@Subcomponent(modules = {ActivityModule.class})
@ScopeActivity
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(BuyActivity activity);
    void inject(SettingsActivity activity);
    void inject(ProductsActivity activity);
    void inject(EditProductActivity activity);
}
