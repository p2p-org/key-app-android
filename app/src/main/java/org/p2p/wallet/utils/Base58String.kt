package org.p2p.wallet.utils

import org.p2p.solanaj.utils.crypto.decodeFromBase58
import org.p2p.solanaj.utils.crypto.encodeToBase58

class Base58String(val base58Value: String) {
    constructor(bytes: ByteArray) : this(base58Value = bytes.copyOf().encodeToBase58())

    fun decodeToBytes(): ByteArray = base58Value.decodeFromBase58()
}

fun String.toBase58Instance(): Base58String = Base58String(this)
fun ByteArray.toBase58Instance(): Base58String = Base58String(this)
