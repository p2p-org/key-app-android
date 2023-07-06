package org.p2p.wallet.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import java.math.BigDecimal
import org.p2p.core.utils.orZero

fun <R> unsafeLazy(initializer: () -> R): Lazy<R> = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun <reified R> List<*>.findInstance(): R? {
    return find { it is R }
        ?.let { it as R }
}

inline fun <E> List<E>.ifSizeNot(expectedSize: Int, defaultValue: (originalList: List<E>) -> List<E>): List<E> {
    return if (this.size != expectedSize) defaultValue.invoke(this) else this
}

inline fun <E> List<E>.ifNotEmpty(action: (originalList: List<E>) -> Unit): List<E> {
    if (isNotEmpty()) action.invoke(this)
    return this
}

// can be used for debug purposes
fun ByteArray.toUIntArray(): String = map(Byte::toUByte).joinToString(prefix = "[", postfix = "]")

fun JsonObject.toByteArray(): ByteArray = toString().toByteArray()

fun ChaCha20Poly1305.processBytesKt(
    inBytes: ByteArray,
    inOff: Int = 0,
    len: Int,
    outBytes: ByteArray,
    outOff: Int = 0
): Int = processBytes(
    inBytes,
    inOff,
    len,
    outBytes,
    outOff
)

fun BigDecimal?.compareTo(other: BigDecimal?): Int = this.orZero().compareTo(other.orZero())

fun JsonArray.getOrNull(index: Int): JsonElement? = if (index >= size()) null else get(index)
