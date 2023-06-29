package org.p2p.core.crypto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.decodeFromBase58
import org.p2p.core.utils.encodeToBase58

@Parcelize
data class Base58String(val base58Value: String) : Parcelable {
    constructor(bytes: ByteArray) : this(base58Value = bytes.copyOf().encodeToBase58())

    fun decodeToBytes(): ByteArray = base58Value.decodeFromBase58()

    override fun toString(): String = base58Value

    fun convertToBase64(): Base64String = decodeToBytes().copyOf().toBase64Instance()
}

fun String.toBase58Instance(): Base58String = Base58String(this)
fun ByteArray.toBase58Instance(): Base58String = Base58String(this)
