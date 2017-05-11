package com.nytimes.android.external.playbillingtester.di;

import com.nytimes.android.external.playbillingtester.BillingServiceStubImpl;
import com.nytimes.android.external.playbillingtester.PlayBillingTesterService;

import dagger.Subcomponent;

@Subcomponent(modules = {ServiceModule.class})
@ScopeService
public interface ServiceComponent {
    void inject(PlayBillingTesterService service);
    void inject(BillingServiceStubImpl impl);
}
