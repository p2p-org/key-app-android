package org.p2p.ethereumkit.internal.core

import org.p2p.ethereumkit.internal.network.EtherscanService
import io.reactivex.Single
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.ProviderEip1155Transaction
import org.p2p.ethereumkit.internal.models.ProviderEip721Transaction
import org.p2p.ethereumkit.internal.models.ProviderInternalTransaction
import org.p2p.ethereumkit.internal.models.ProviderTokenTransaction
import org.p2p.ethereumkit.internal.models.ProviderTransaction

class EtherscanTransactionProvider(
    private val etherscanService: EtherscanService,
    private val address: EthAddress
) : ITransactionProvider {

    override fun getTransactions(startBlock: Long): Single<List<ProviderTransaction>> {
        return etherscanService.getTransactionList(address, startBlock)
            .map { response ->
                response.result.distinctBy { it["hash"] }.mapNotNull { tx ->
                    try {
                        val blockNumber = tx.getValue("blockNumber").toLong()
                        val timestamp = tx.getValue("timeStamp").toLong()
                        val hash = tx.getValue("hash").hexStringToByteArray()
                        val nonce = tx.getValue("nonce").toLong()
                        val blockHash = tx["blockHash"]?.hexStringToByteArray()
                        val transactionIndex = tx.getValue("transactionIndex").toInt()
                        val from = EthAddress(tx.getValue("from"))
                        val to = getAddressOrNull(tx["to"])
                        val value = tx.getValue("value").toBigInteger()
                        val gasLimit = tx.getValue("gas").toLong()
                        val gasPrice = tx.getValue("gasPrice").toLong()
                        val isError = tx["isError"]?.toIntOrNull()
                        val txReceiptStatus = tx["txreceipt_status"]?.toIntOrNull()
                        val input = tx.getValue("input").hexStringToByteArray()
                        val cumulativeGasUsed = tx["cumulativeGasUsed"]?.toLongOrNull()
                        val gasUsed = tx["gasUsed"]?.toLongOrNull()

                        ProviderTransaction(
                            blockNumber, timestamp, hash, nonce, blockHash, transactionIndex, from, to, value, gasLimit, gasPrice,
                            isError, txReceiptStatus, input, cumulativeGasUsed, gasUsed
                        )

                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }
    }

    override fun getInternalTransactions(startBlock: Long): Single<List<ProviderInternalTransaction>> {
        return etherscanService.getInternalTransactionList(address, startBlock)
            .map { response ->
                response.result.mapNotNull { internalTx ->
                    try {
                        val hash = internalTx.getValue("hash").hexStringToByteArray()
                        val blockNumber = internalTx.getValue("blockNumber").toLong()
                        val timestamp = internalTx.getValue("timeStamp").toLong()
                        val from = EthAddress(internalTx.getValue("from"))
                        val to = EthAddress(internalTx.getValue("to"))
                        val value = internalTx.getValue("value").toBigInteger()
                        val traceId = internalTx.getValue("traceId")

                        ProviderInternalTransaction(hash, blockNumber, timestamp, from, to, value, traceId)
                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }
    }

    override fun getInternalTransactionsAsync(hash: ByteArray): Single<List<ProviderInternalTransaction>> {
        return etherscanService.getInternalTransactionsAsync(hash)
            .map { response ->
                response.result.mapNotNull { internalTx ->
                    try {
                        val blockNumber = internalTx.getValue("blockNumber").toLong()
                        val timestamp = internalTx.getValue("timeStamp").toLong()
                        val from = EthAddress(internalTx.getValue("from"))
                        val to = EthAddress(internalTx.getValue("to"))
                        val value = internalTx.getValue("value").toBigInteger()
                        val traceId = internalTx.getValue("traceId")

                        ProviderInternalTransaction(hash, blockNumber, timestamp, from, to, value, traceId)
                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }
    }

    override fun getTokenTransactions(startBlock: Long): Single<List<ProviderTokenTransaction>> {
        return etherscanService.getTokenTransactions(address, startBlock)
            .map { response ->
                response.result.mapNotNull { tx ->
                    try {
                        val blockNumber = tx.getValue("blockNumber").toLong()
                        val timestamp = tx.getValue("timeStamp").toLong()
                        val hash = tx.getValue("hash").hexStringToByteArray()
                        val nonce = tx.getValue("nonce").toLong()
                        val blockHash = tx.getValue("blockHash").hexStringToByteArray()
                        val from = EthAddress(tx.getValue("from"))
                        val contractAddress = EthAddress(tx.getValue("contractAddress"))
                        val to = EthAddress(tx.getValue("to"))
                        val value = tx.getValue("value").toBigInteger()
                        val tokenName = tx.getValue("tokenName")
                        val tokenSymbol = tx.getValue("tokenSymbol")
                        val tokenDecimal = tx.getValue("tokenDecimal").toInt()
                        val transactionIndex = tx.getValue("transactionIndex").toInt()
                        val gasLimit = tx.getValue("gas").toLong()
                        val gasPrice = tx.getValue("gasPrice").toLong()
                        val gasUsed = tx.getValue("gasUsed").toLong()
                        val cumulativeGasUsed = tx.getValue("cumulativeGasUsed").toLong()

                        ProviderTokenTransaction(
                            blockNumber, timestamp, hash, nonce, blockHash, from, contractAddress, to, value, tokenName, tokenSymbol, tokenDecimal,
                            transactionIndex, gasLimit, gasPrice, gasUsed, cumulativeGasUsed
                        )

                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }
    }

    override fun getEip721Transactions(startBlock: Long): Single<List<ProviderEip721Transaction>> {
        return etherscanService.getEip721Transactions(address, startBlock)
            .map { response ->
                response.result.mapNotNull { tx ->
                    try {
                        val blockNumber = tx.getValue("blockNumber").toLong()
                        val timestamp = tx.getValue("timeStamp").toLong()
                        val hash = tx.getValue("hash").hexStringToByteArray()
                        val nonce = tx.getValue("nonce").toLong()
                        val blockHash = tx.getValue("blockHash").hexStringToByteArray()
                        val from = EthAddress(tx.getValue("from"))
                        val contractAddress = EthAddress(tx.getValue("contractAddress"))
                        val to = EthAddress(tx.getValue("to"))
                        val tokenId = tx.getValue("tokenID").toBigInteger()
                        val tokenName = tx.getValue("tokenName")
                        val tokenSymbol = tx.getValue("tokenSymbol")
                        val tokenDecimal = tx.getValue("tokenDecimal").toInt()
                        val transactionIndex = tx.getValue("transactionIndex").toInt()
                        val gasLimit = tx.getValue("gas").toLong()
                        val gasPrice = tx.getValue("gasPrice").toLong()
                        val gasUsed = tx.getValue("gasUsed").toLong()
                        val cumulativeGasUsed = tx.getValue("cumulativeGasUsed").toLong()

                        ProviderEip721Transaction(
                            blockNumber = blockNumber,
                            timestamp = timestamp,
                            hash = hash,
                            nonce = nonce,
                            blockHash = blockHash,
                            transactionIndex = transactionIndex,
                            gasLimit = gasLimit,
                            gasPrice = gasPrice,
                            gasUsed = gasUsed,
                            cumulativeGasUsed = cumulativeGasUsed,
                            contractAddress = contractAddress,
                            from = from,
                            to = to,
                            tokenId = tokenId,
                            tokenName = tokenName,
                            tokenSymbol = tokenSymbol,
                            tokenDecimal = tokenDecimal
                        )
                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }
    }

    override fun getEip1155Transactions(startBlock: Long): Single<List<ProviderEip1155Transaction>> {
        return etherscanService.getEip1155Transactions(address, startBlock)
            .map { response ->
                response.result.mapNotNull { tx ->
                    try {
                        val blockNumber = tx.getValue("blockNumber").toLong()
                        val timestamp = tx.getValue("timeStamp").toLong()
                        val hash = tx.getValue("hash").hexStringToByteArray()
                        val nonce = tx.getValue("nonce").toLong()
                        val blockHash = tx.getValue("blockHash").hexStringToByteArray()
                        val from = EthAddress(tx.getValue("from"))
                        val contractAddress = EthAddress(tx.getValue("contractAddress"))
                        val to = EthAddress(tx.getValue("to"))

                        val tokenId = tx.getValue("tokenID").toBigInteger()
                        val tokenValue = tx.getValue("tokenValue").toInt()
                        val tokenName = tx.getValue("tokenName")
                        val tokenSymbol = tx.getValue("tokenSymbol")
                        val transactionIndex = tx.getValue("transactionIndex").toInt()
                        val gasLimit = tx.getValue("gas").toLong()
                        val gasPrice = tx.getValue("gasPrice").toLong()
                        val gasUsed = tx.getValue("gasUsed").toLong()
                        val cumulativeGasUsed = tx.getValue("cumulativeGasUsed").toLong()

                        ProviderEip1155Transaction(
                            blockNumber = blockNumber,
                            timestamp = timestamp,
                            hash = hash,
                            nonce = nonce,
                            blockHash = blockHash,
                            transactionIndex = transactionIndex,
                            gasLimit = gasLimit,
                            gasPrice = gasPrice,
                            gasUsed = gasUsed,
                            cumulativeGasUsed = cumulativeGasUsed,
                            contractAddress = contractAddress,
                            from = from,
                            to = to,
                            tokenId = tokenId,
                            tokenValue = tokenValue,
                            tokenName = tokenName,
                            tokenSymbol = tokenSymbol
                        )
                    } catch (throwable: Throwable) {
                        null
                    }
                }
            }

    }

    private fun getAddressOrNull(addressString: String?): EthAddress? =
        if (!addressString.isNullOrEmpty()) EthAddress(addressString) else null

}
