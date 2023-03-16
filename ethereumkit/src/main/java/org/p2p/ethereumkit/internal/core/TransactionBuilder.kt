package org.p2p.ethereumkit.internal.core

import org.p2p.core.wrapper.eth.CryptoUtils
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.*
import org.p2p.ethereumkit.internal.spv.core.toBigInteger
import org.p2p.core.wrapper.eth.rlp.RLP

class TransactionBuilder(
    private val address: EthAddress,
    private val chainId: Int,
) {

    fun transaction(rawTransaction: RawTransaction, signature: Signature): Transaction {
        val transactionHash = CryptoUtils.sha3(encode(rawTransaction, signature))
        var maxFeePerGas: Long? = null
        var maxPriorityFeePerGas: Long? = null

        if (rawTransaction.gasPrice is GasPrice.Eip1559) {
            maxFeePerGas = rawTransaction.gasPrice.maxFeePerGas
            maxPriorityFeePerGas = rawTransaction.gasPrice.maxPriorityFeePerGas
        }

        return Transaction(
            hash = transactionHash,
            timestamp = System.currentTimeMillis() / 1000,
            nonce = rawTransaction.nonce,
            input = rawTransaction.data,
            from = address,
            to = rawTransaction.to,
            value = rawTransaction.value,
            gasPrice = rawTransaction.gasPrice.max,
            maxFeePerGas = maxFeePerGas,
            maxPriorityFeePerGas = maxPriorityFeePerGas,
            gasLimit = rawTransaction.gasLimit,
            isFailed = false,
        )
    }

    fun encode(rawTransaction: RawTransaction, signature: Signature): ByteArray =
        encode(rawTransaction, signature, chainId)

    companion object {

        fun encode(rawTransaction: RawTransaction, signature: Signature?, chainId: Int = 1): ByteArray {
            val signatureArray = signature?.let {
                arrayOf(
                    RLP.encodeInt(it.v),
                    RLP.encodeBigInteger(it.r.toBigInteger()),
                    RLP.encodeBigInteger(it.s.toBigInteger())
                )
            } ?: arrayOf()

            val elements = arrayOf(
                RLP.encodeInt(chainId),
                RLP.encodeLong(rawTransaction.nonce),
                RLP.encodeLong(rawTransaction.gasPrice.value),
                RLP.encodeLong(rawTransaction.gasLimit),
                RLP.encodeElement(rawTransaction.to.raw),
                RLP.encodeBigInteger(rawTransaction.value),
                RLP.encodeElement(rawTransaction.data)
            ) + signatureArray

            return RLP.encodeList(*elements)
        }
    }
}

fun decode(transaction: ByteArray, address: EthAddress): RawTransaction {
    val ar1 = transaction[0]
    return RawTransaction(
        nonce = RLP.decodeLong(transaction, 0),
        gasPrice = GasPrice.Legacy(RLP.decodeLong(transaction, 1)),
        gasLimit = RLP.decodeLong(transaction, 2),
        to = address,
        value = RLP.decodeInt(RLP.decodeToOneItem(transaction, 4)).toBigInteger(),
        data = RLP.decodeToOneItem(transaction, 5).rlpData ?: byteArrayOf()
    )
}
