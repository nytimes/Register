package com.nytimes.android.external.register

import com.google.common.base.Optional
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.PrivateKey
import java.security.Signature
import java.security.SignatureException

class Signer(val privateKey: Optional<PrivateKey>?, val signature: Optional<Signature>?) {

    @Throws(InvalidKeyException::class, SignatureException::class)
    fun signData(unsignedData: String): String {
        return signature?.isPresent?.let {
            signature.get().initSign(privateKey?.orNull())
            signature.get().update(unsignedData.toByteArray(StandardCharsets.UTF_8))
            String(signature.get().sign(), StandardCharsets.UTF_8)
        } ?: ""
    }
}
