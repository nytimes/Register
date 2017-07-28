package com.nytimes.android.external.register.bundle;

import com.nytimes.android.external.register.APIOverrides;
import com.nytimes.android.external.registerlib.GoogleUtil;

public abstract class BaseResponse {

    protected final APIOverrides apiOverrides;

    public BaseResponse(APIOverrides apiOverrides) {
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
