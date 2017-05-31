package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.nytimes.android.external.playbillingtester.APIOverrides;

public abstract class BaseBundleBuilder extends BaseResponse {

    protected Bundle bundle;

    public BaseBundleBuilder(APIOverrides apiOverrides) {
        super(apiOverrides);
    }

}
