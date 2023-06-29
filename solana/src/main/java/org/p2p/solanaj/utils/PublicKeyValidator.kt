package org.p2p.solanaj.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.core.utils.Base58Utils

object PublicKeyValidator {
    private const val REGEX = "[1-9A-HJ-NP-Za-km-z]{32,44}"

    fun isValid(publicKey: String): Boolean =
        Regex(REGEX).matches(publicKey) && Base58Utils.decode(publicKey).size == PublicKey.PUBLIC_KEY_LENGTH

    fun isValid(publicKey: ByteArray): Boolean =
        Regex(REGEX).matches(Base58Utils.encode(publicKey)) && publicKey.size == PublicKey.PUBLIC_KEY_LENGTH
}
