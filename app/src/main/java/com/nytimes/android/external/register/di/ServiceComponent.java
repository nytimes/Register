package com.nytimes.android.external.register.di;

import com.nytimes.android.external.register.BillingServiceStubImpl;
import com.nytimes.android.external.register.RegisterService;

import dagger.Subcomponent;

@Subcomponent(modules = {ServiceModule.class})
@ScopeService
public interface ServiceComponent {
    void inject(RegisterService service);
    void inject(BillingServiceStubImpl impl);
}
