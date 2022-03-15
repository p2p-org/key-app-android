package org.p2p.solanaj.utils

import org.bitcoinj.core.Base58
import org.p2p.solanaj.core.PublicKey

object PublicKeyValidator {
    private const val REGEX = "[1-9A-HJ-NP-Za-km-z]{32,44}"

    @Throws(IllegalArgumentException::class)
    fun validate(publicKey: String) = require(
        Regex(REGEX).matches(publicKey) && Base58.decode(publicKey).size == PublicKey.PUBLIC_KEY_LENGTH
    ) {
        { "Invalid public key input[String]" }
    }

    @Throws(IllegalArgumentException::class)
    fun validate(publicKey: ByteArray) = require(
        Regex(REGEX).matches(Base58.encode(publicKey)) && publicKey.size == PublicKey.PUBLIC_KEY_LENGTH
    ) {
        { "Invalid public key input[ByteArray]" }
    }
}