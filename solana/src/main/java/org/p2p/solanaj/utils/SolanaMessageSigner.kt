package org.p2p.solanaj.utils

import org.p2p.core.crypto.Base64String
import org.p2p.core.crypto.toBase64Instance

class SolanaMessageSigner {
    fun signMessage(message: ByteArray, keyPair: ByteArray): Base64String {
        return TweetNaclFast.Signature(byteArrayOf(), keyPair.copyOf())
            .detached(message.copyOf())
            .toBase64Instance()
    }

    fun signMessage(message: String, keyPair: ByteArray): Base64String {
        return signMessage(message.toByteArray(), keyPair)
    }
}
