package org.p2p.wallet.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.solanaj.utils.crypto.decodeFromBase58
import org.p2p.solanaj.utils.crypto.encodeToBase58
import org.p2p.solanaj.utils.crypto.toBase64Instance

@Parcelize
data class Base58String(val base58Value: String) : Parcelable {
    constructor(bytes: ByteArray) : this(base58Value = bytes.copyOf().encodeToBase58())

    fun decodeToBytes(): ByteArray = base58Value.decodeFromBase58()

    override fun toString(): String = base58Value

    fun convertToBase64(): Base64String = decodeToBytes().copyOf().toBase64Instance()
}

fun String.toBase58Instance(): Base58String = Base58String(this)
fun PublicKey.toBase58Instance(): Base58String = Base58String(this.toBase58())
fun ByteArray.toBase58Instance(): Base58String = Base58String(this)
