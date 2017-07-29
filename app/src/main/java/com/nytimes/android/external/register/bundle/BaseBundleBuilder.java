package com.nytimes.android.external.register.bundle;

import android.os.Bundle;

import com.nytimes.android.external.register.APIOverrides;

public abstract class BaseBundleBuilder extends BaseResponse {

    protected Bundle bundle;

    public BaseBundleBuilder(APIOverrides apiOverrides) {
        super(apiOverrides);
    }

}
