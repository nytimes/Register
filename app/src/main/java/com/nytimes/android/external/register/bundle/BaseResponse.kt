package com.nytimes.android.external.register.bundle

import com.nytimes.android.external.register.APIOverrides
import com.nytimes.android.external.registerlib.GoogleUtil

abstract class BaseResponse(protected val apiOverrides: APIOverrides) {

    abstract fun rawResponseCode(): Int

    fun responseCode(): Int {
        var responseCode = rawResponseCode()
        if (responseCode == APIOverrides.RESULT_DEFAULT) {
            responseCode = GoogleUtil.RESULT_OK
        }
        return responseCode
    }

}
