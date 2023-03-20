package org.p2p.ethereumkit.internal.models

import org.p2p.ethereumkit.internal.core.toHexString
import java.math.BigInteger
import org.p2p.core.wrapper.eth.EthAddress

class RawTransaction(
    val gasPrice: GasPrice,
    val gasLimit: Long,
    val to: EthAddress,
    val value: BigInteger,
    val nonce: Long,
    val data: ByteArray = ByteArray(0),
    var chainId: Int? = null
) {

    override fun toString(): String {
        return "RawTransaction [gasPrice: $gasPrice; gasLimit: $gasLimit; to: $to; value: $value; data: ${data.toHexString()}; nonce: $nonce]"
    }
}
