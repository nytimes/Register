package com.nytimes.android.external.playbillingtester.bundle;

import com.nytimes.android.external.playbillingtester.APIOverridesAndPurchases;
import com.nytimes.android.external.playbillingtesterlib.GoogleUtil;

public abstract class BaseResponse {

    protected final APIOverridesAndPurchases apiOverridesAndPurchases;

    public BaseResponse(APIOverridesAndPurchases apiOverridesAndPurchases) {
        this.apiOverridesAndPurchases = apiOverridesAndPurchases;
    }

    protected abstract int rawResponseCode();

    public int responseCode() {
        int responseCode = rawResponseCode();
        if (responseCode == APIOverridesAndPurchases.RESULT_DEFAULT) {
            responseCode = GoogleUtil.RESULT_OK;
        }
        return responseCode;
    }

}
