package com.nytimes.android.external.register.bundle

import android.os.Bundle

import com.nytimes.android.external.register.APIOverrides

abstract class BaseBundleBuilder(apiOverrides: APIOverrides) : BaseResponse(apiOverrides) {

    protected var bundle: Bundle = Bundle()

}
