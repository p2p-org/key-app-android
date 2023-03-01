package org.p2p.ethereumkit.core.rollup

import org.p2p.ethereumkit.contracts.ContractMethod
import org.p2p.ethereumkit.core.EthereumKit
import org.p2p.ethereumkit.core.TransactionBuilder
import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.GasPrice
import org.p2p.ethereumkit.models.RawTransaction
import org.p2p.ethereumkit.spv.core.toBigInteger
import io.reactivex.Single
import java.math.BigInteger

class L1FeeProvider(
        private val evmKit: EthereumKit,
        private val contractAddress: EthAddress
) {

    class L1FeeMethod(val transaction: ByteArray) : ContractMethod() {
        override val methodSignature = "getL1Fee(bytes)"
        override fun getArguments() = listOf(transaction)
    }

    fun getL1Fee(gasPrice: GasPrice, gasLimit: Long, to: EthAddress, value: BigInteger, data: ByteArray): Single<BigInteger> {
        val rawTransaction = RawTransaction(gasPrice, gasLimit, to, value, 1, data)
        val encoded = TransactionBuilder.encode(rawTransaction, null, evmKit.chain.id)
        val feeMethodABI = L1FeeMethod(encoded).encodedABI()

        return evmKit.call(contractAddress, feeMethodABI)
                .map { it.sliceArray(IntRange(0, 31)).toBigInteger() }
    }

}
