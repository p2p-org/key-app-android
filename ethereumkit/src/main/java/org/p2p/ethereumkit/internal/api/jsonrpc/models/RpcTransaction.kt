package org.p2p.ethereumkit.internal.api.jsonrpc.models

import com.google.gson.annotations.SerializedName
import org.p2p.ethereumkit.internal.models.EthAddress
import java.math.BigInteger

class RpcTransaction(
    val hash: ByteArray,
    val nonce: Long,
    val blockHash: ByteArray?,
    val blockNumber: Long?,
    val transactionIndex: Int?,
    val from: EthAddress,
    val to: EthAddress?,
    val value: BigInteger,
    val gasPrice: Long,
    val maxFeePerGas: Long?,
    val maxPriorityFeePerGas: Long?,
    @SerializedName("gas")
        val gasLimit: Long,
    val input: ByteArray
)
