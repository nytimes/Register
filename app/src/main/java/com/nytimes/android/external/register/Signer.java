package com.nytimes.android.external.register;

import android.support.annotation.Nullable;

import com.google.common.base.Optional;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.inject.Inject;

public class Signer {

    @Inject
    @Nullable
    Optional<PrivateKey> privateKey;

    @Inject
    @Nullable
    Optional<Signature> signature;

    public Signer(@Nullable Optional<PrivateKey> privateKey, @Nullable Optional<Signature> signature) {
        this.privateKey = privateKey;
        this.signature = signature;
    }

    public String signData(String unsignedData) throws InvalidKeyException, SignatureException {
        String signedData = "";
        if (signature.isPresent()) {
            signature.get().initSign(privateKey.orNull());
            signature.get().update(unsignedData.getBytes(StandardCharsets.UTF_8));
            signedData = new String(signature.get().sign(), StandardCharsets.UTF_8);
        }
        return signedData;
    }
}
