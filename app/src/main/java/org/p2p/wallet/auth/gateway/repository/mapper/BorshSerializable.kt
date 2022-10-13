package org.p2p.wallet.auth.gateway.repository.mapper

import com.google.gson.JsonObject
import org.near.borshj.BorshBuffer
import timber.log.Timber

private const val DEFAULT_BORSH_CAPACITY = 4096

/**
 * Own interface instead of Borsh one
 * we are serializing our structures (aka classes) like Parcelable - write your own algorithm
 * see implementations for examples
 *
 * https://github.com/near/borshj/issues/3
 */
interface BorshSerializable {
    fun getBorshBuffer(): BorshBuffer = BorshBuffer.allocate(DEFAULT_BORSH_CAPACITY)
    fun serializeSelf(): ByteArray
}

fun BorshBuffer.write(vararg objects: Any): BorshBuffer {
    val validatedObjects: List<Any> = objects.map { if (it is JsonObject) it.toString() else it }
    validatedObjects.forEach {
        Timber.tag("BorshBuffer").d("Written to borsh: $it")
        this.write(it)
    }
    return this
}
