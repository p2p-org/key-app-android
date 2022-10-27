package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.history.interactor.stream.HistoryStreamItem
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.rpc.RpcConstants
import org.p2p.wallet.rpc.api.RpcHistoryApi
import timber.log.Timber

class TransactionDetailsRpcRepository(
    private val rpcApi: RpcHistoryApi,
    private val transactionParsingContext: TransactionParsingContext
) : TransactionDetailsRemoteRepository {

    override suspend fun getTransactions(
        userPublicKey: String,
        transactionSignatures: List<HistoryStreamItem>
    ): List<TransactionDetails> {

        val encoding = buildMap {
            this[RpcConstants.REQUEST_PARAMETER_KEY_ENCODING] =
                RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED
            this[RpcConstants.REQUEST_PARAMETER_KEY_COMMITMENT] =
                RpcConstants.REQUEST_PARAMETER_VALUE_CONFIRMED
        }
        val requestsBatch = transactionSignatures.map { streamItem ->

            val signature = streamItem.streamSource
                ?.signature
                ?: return emptyList()

            val params = listOf(signature, encoding)

            RpcRequest(
                method = RpcConstants.REQUEST_METHOD_VALUE_GET_CONFIRMED_TRANSACTIONS,
                params = params
            )
        }

        val transactions = rpcApi.getConfirmedTransactions(requestsBatch)
            .map { it.result }

        return fromNetworkToDomain(transactions).onEach { transactionDetails ->
            val signatureItem =
                transactionSignatures.first { it.streamSource?.signature == transactionDetails.signature }
            transactionDetails.status = signatureItem.streamSource?.status
            transactionDetails.account = signatureItem.account
        }
    }

    private suspend fun fromNetworkToDomain(
        transactions: List<ConfirmedTransactionRootResponse>
    ): List<TransactionDetails> {
        val transactionDetails = mutableListOf<TransactionDetails>()
        transactions.forEach { transaction ->

            when (val parsingResult = transactionParsingContext.parseTransaction(transaction)) {
                is ParsingResult.Transaction -> {
                    transactionDetails.addAll(parsingResult.details)
                }
                is ParsingResult.Error -> {
                    Timber.i("Error on parsing transaction ${parsingResult.error.message}")
                }
            }
        }
        transactionDetails.forEach { data ->
            data.error = transactions.firstOrNull {
                it.transaction?.getTransactionId() == data.signature
            }
                ?.meta
                ?.error
        }
        return transactionDetails.distinctBy { it.signature }
    }
}
