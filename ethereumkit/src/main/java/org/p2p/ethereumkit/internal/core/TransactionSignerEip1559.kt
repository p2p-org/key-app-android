package org.p2p.ethereumkit.internal.core

import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import org.web3j.crypto.TransactionDecoder
import org.web3j.crypto.TransactionEncoder
import java.math.BigInteger
import org.p2p.core.wrapper.HexString
import org.p2p.core.wrapper.eth.toInt
import org.p2p.ethereumkit.internal.models.Signature

class TransactionSignerEip1559(
    private val privateKey: BigInteger,
) {

    fun sign(encodedTransaction: HexString, chainId: Long = 1L): Signature {
        val decodedValue = TransactionDecoder.decode(encodedTransaction.rawValue)
        val encodedValue = TransactionEncoder.encode(decodedValue)

        val keyPair = ECKeyPair.create(privateKey)
        val signatureData = Sign.signMessage(encodedValue, keyPair)
        val signature = TransactionEncoder.createEip155SignatureData(signatureData, chainId)
        return Signature(
            v = signature.v.toInt(),
            r = signature.r,
            s = signature.s
        )
    }
}
