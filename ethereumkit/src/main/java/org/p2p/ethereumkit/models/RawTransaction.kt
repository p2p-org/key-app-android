package org.p2p.ethereumkit.models

import org.p2p.ethereumkit.core.toHexString
import java.math.BigInteger

class RawTransaction(
    val gasPrice: GasPrice,
    val gasLimit: Long,
    val to: EthAddress,
    val value: BigInteger,
    val nonce: Long,
    val data: ByteArray = ByteArray(0)
) {

    override fun toString(): String {
        return "RawTransaction [gasPrice: $gasPrice; gasLimit: $gasLimit; to: $to; value: $value; data: ${data.toHexString()}; nonce: $nonce]"
    }
}
