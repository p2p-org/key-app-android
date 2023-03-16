package org.p2p.ethereumkit.internal.models

import org.p2p.ethereumkit.internal.core.toHexString
import java.math.BigInteger
import java.util.*
import org.p2p.core.wrapper.eth.EthAddress

data class TransactionData(
    val to: EthAddress,
    val value: BigInteger,
    val input: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other is TransactionData -> to == other.to && value == other.value && input.contentEquals(other.input)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(to, value, input)
    }

    override fun toString(): String {
        return "TransactionData {to: ${to.hex}, value: $value, input: ${input.toHexString()}}"
    }

}
