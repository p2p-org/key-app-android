package org.p2p.wallet.sdk

import androidx.annotation.Keep

@Keep
class RelaySdk {

    external fun signTransaction(
        transaction: String,
        keypair: String,
        blockhash: String
    ): String
}
