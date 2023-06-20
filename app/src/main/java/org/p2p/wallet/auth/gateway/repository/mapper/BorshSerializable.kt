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
 *
 * u8 - byte
 * [u8] - ByteArray
 * u16 - short
 * u32 - int
 * u64 - long
 * u128 - BigInteger
 * f32 - float
 * f64     double
 * str - String
 * map - Map<*,*>
 * set - Set<*>
 * struct - Object / class
 */
interface BorshSerializable {
    fun getBorshBuffer(): BorshBuffer = BorshBuffer.allocate(DEFAULT_BORSH_CAPACITY)
    fun serializeSelf(): ByteArray
}

fun BorshBuffer.write(vararg objects: Any): BorshBuffer {
    val validatedObjects: List<Any> = objects.map { if (it is JsonObject) it.toString() else it }
    validatedObjects.forEach {
        if (it is ByteArray) {
            this.writeFixedArray(it)
        } else {
            this.write(it)
        }
        Timber.tag("BorshBuffer").d("Written to borsh: $it")
    }
    return this
}
