package org.p2p.wallet.infrastructure.network.interceptor

import java.util.Date
import org.p2p.solanaj.utils.TweetNaclFast
import org.p2p.solanaj.utils.crypto.toBase64Instance

class StrigaHeaderSignatureGenerator {
    /**
     * @return format: unix_time:unix_time_signed_by_private_key
     */
    fun generate(userKeyPair: ByteArray): String {
        val currentUnixTime = Date().time.toString()

        val signedUnixTime = TweetNaclFast.Signature(byteArrayOf(), userKeyPair.copyOf())
            .detached(currentUnixTime.toByteArray())
            .toBase64Instance()
            .base64Value

        return "$currentUnixTime:$signedUnixTime"
    }
}
