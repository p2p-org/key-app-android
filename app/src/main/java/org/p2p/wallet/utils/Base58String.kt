package org.p2p.wallet.utils

import org.p2p.solanaj.utils.crypto.Base58Utils

class Base58String(val value: String) {
    constructor(bytes: ByteArray) : this(Base58Utils.encode(bytes))
}

fun String.toBase58Instance(): Base58String {
    return Base58String(this)
}

fun ByteArray.toBase58Instance(): Base58String {
    return Base58String(this)
}
