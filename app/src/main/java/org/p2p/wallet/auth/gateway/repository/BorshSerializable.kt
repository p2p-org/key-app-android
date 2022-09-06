package org.p2p.wallet.auth.gateway.repository

import org.near.borshj.BorshBuffer

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
    objects.forEach { this.write(it) }
    return this
}
