package org.p2p.ethereumkit.internal.spv.net.les.messages

import org.p2p.ethereumkit.internal.core.toHexString
import org.p2p.ethereumkit.internal.models.RawTransaction
import org.p2p.ethereumkit.internal.models.Signature
import org.p2p.ethereumkit.internal.spv.net.IOutMessage
import org.p2p.core.wrapper.eth.rlp.RLP
import java.math.BigInteger

class SendTransactionMessage(val requestID: Long, val rawTransaction: RawTransaction, val signature: Signature) : IOutMessage {

    override fun encoded(): ByteArray {
        return RLP.encodeList(
                RLP.encodeBigInteger(BigInteger.valueOf(requestID)),
                RLP.encodeList(
                        RLP.encodeList(
                                RLP.encodeLong(rawTransaction.nonce),
                                RLP.encodeLong(rawTransaction.gasPrice.max), // TODO: Use 1559 RLP encoding
                                RLP.encodeLong(rawTransaction.gasLimit),
                                RLP.encodeElement(rawTransaction.to.raw),
                                RLP.encodeBigInteger(rawTransaction.value),
                                RLP.encodeElement(rawTransaction.data),
                                RLP.encodeInt(signature.v),
                                RLP.encodeElement(signature.r),
                                RLP.encodeElement(signature.s)
                        )
                )
        )
    }

    override fun toString(): String {
        return "SendTransaction [requestId: $requestID; nonce: ${rawTransaction.nonce}; gasPrice: ${rawTransaction.gasPrice}; gasLimit: ${rawTransaction.gasLimit}; to: ${rawTransaction.to}; " +
                "value: ${rawTransaction.value}; data: ${rawTransaction.data}; " +
                "v: ${signature.v}; r: ${signature.r.toHexString()}; s: ${signature.s.toHexString()}]"
    }

}
