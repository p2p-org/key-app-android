package org.p2p.ethereumkit.internal.core

import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import java.math.BigInteger
import org.p2p.core.wrapper.eth.CryptoUtils
import org.p2p.ethereumkit.internal.models.Signature

class TransactionSignerLegacy(
    private val privateKey: BigInteger,
    private val chainId: Int,
) {

    fun signatureLegacy(rawTransaction: RawTransaction): Signature {
        val hex = TransactionEncoder.encode(rawTransaction,1L)
        val rawTransactionHash = CryptoUtils.sha3(hex)
        val sign = CryptoUtils.ellipticSign(rawTransactionHash, privateKey)
        return signatureLegacy(sign)
    }

    private fun signatureLegacy(signatureData: ByteArray): Signature {
        return Signature(
            v = signatureData[64] + if (chainId == 0) 27 else (35 + 2 * chainId),
            r = signatureData.copyOfRange(0, 32),
            s = signatureData.copyOfRange(32, 64)
        )
    }

}
