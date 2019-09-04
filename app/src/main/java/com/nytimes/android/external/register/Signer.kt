package com.nytimes.android.external.register

import com.google.common.base.Optional
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.PrivateKey
import java.security.Signature
import java.security.SignatureException

class Signer(private val privateKey: Optional<PrivateKey>?,
             private val signature: Optional<Signature>?) {

    @Throws(InvalidKeyException::class, SignatureException::class)
    fun signData(unsignedData: String): String {
        return signature.isPresent {
            it.initSign(privateKey?.orNull())
            it.update(unsignedData.toByteArray(StandardCharsets.UTF_8))
            String(it.sign(), StandardCharsets.UTF_8)
        } ?: ""
    }
}
