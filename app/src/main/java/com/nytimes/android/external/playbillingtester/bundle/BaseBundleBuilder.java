package com.nytimes.android.external.playbillingtester.bundle;

import android.os.Bundle;

import com.nytimes.android.external.playbillingtester.APIOverrides;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

public abstract class BaseBundleBuilder {

    protected Bundle bundle;
    protected final APIOverrides apiOverrides;

    public BaseBundleBuilder(APIOverrides apiOverrides) {
        this.apiOverrides = apiOverrides;
    }

    protected abstract int rawResponseCode();

    public int responseCode() {
        int responseCode = rawResponseCode();
        if (responseCode == APIOverrides.RESULT_DEFAULT) {
            responseCode = GoogleUtil.RESULT_OK;
        }
        return responseCode;
    }
}
