package org.p2p.ethereumkit.internal.api.jsonrpc.models

import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.TransactionLog

class RpcTransactionReceipt(
    val transactionHash: ByteArray,
    val transactionIndex: Int,
    val blockHash: ByteArray,
    val blockNumber: Long,
    val from: EthAddress,
    val to: EthAddress?,
    val effectiveGasPrice: Long,
    val cumulativeGasUsed: Long,
    val gasUsed: Long,
    val contractAddress: EthAddress?,
    val logs: List<TransactionLog>,
    val logsBloom: ByteArray,
    val root: ByteArray?,
    val status: Int?
)
