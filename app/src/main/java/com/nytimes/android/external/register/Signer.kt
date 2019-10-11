package com.nytimes.android.external.register

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.PrivateKey
import java.security.Signature
import java.security.SignatureException

class Signer(private val privateKey: PrivateKey?,
             private val signature: Signature?) {

    @Throws(InvalidKeyException::class, SignatureException::class)
    fun signData(unsignedData: String): String {
        return signature?.let {
            it.initSign(privateKey)
            it.update(unsignedData.toByteArray(StandardCharsets.UTF_8))
            String(it.sign(), StandardCharsets.UTF_8)
        } ?: ""
    }
}
