package org.p2p.solanaj.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.crypto.Base58Utils

object PublicKeyValidator {
    private const val REGEX = "[1-9A-HJ-NP-Za-km-z]{32,44}"

    @Throws(IllegalArgumentException::class)
    fun validate(publicKey: String) {
        require(Regex(REGEX).matches(publicKey) && Base58Utils.decode(publicKey).size == PublicKey.PUBLIC_KEY_LENGTH) {
            "Invalid public key input[String]"
        }
    }

    @Throws(IllegalArgumentException::class)
    fun validate(publicKey: ByteArray) {
        require(Regex(REGEX).matches(Base58Utils.encode(publicKey)) && publicKey.size == PublicKey.PUBLIC_KEY_LENGTH) {
            "Invalid public key input[ByteArray]"
        }
    }
}
