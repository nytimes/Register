package com.nytimes.android.external.playbillingtester.di;

import com.nytimes.android.external.playbillingtester.BuyActivity;
import com.nytimes.android.external.playbillingtester.MainActivity;

import dagger.Subcomponent;

@Subcomponent(modules = {ActivityModule.class})
@ScopeActivity
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(BuyActivity activity);

}
