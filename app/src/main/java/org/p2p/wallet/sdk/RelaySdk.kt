package org.p2p.wallet.sdk

import androidx.annotation.Keep

@Keep
class RelaySdk {
    init {
        System.loadLibrary("keyapp_sdk_android")
    }

    external fun signTransaction(
        transaction: String, // in base58 or base64
        keypair: String, // in base58 (32 priv_key + 32 pub_key)
        blockhash: String // empty_string if exists in transaction param
    ): String
}
