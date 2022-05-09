package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.wallet.history.interactor.mapper.SolanaInstructionParser
import org.p2p.wallet.rpc.RpcConstants
import org.p2p.wallet.rpc.api.RpcHistoryApi
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class TransactionDetailsRpcRepository(
    private val rpcApi: RpcHistoryApi,
    private val userInteractor: UserInteractor,
) : TransactionDetailsRemoteRepository {

    override suspend fun getTransactions(userPublicKey: String, signatures: List<String>): List<TransactionDetails> {
        val requestsBatch = signatures.map { signature ->
            val encoding = buildMap {
                this[RpcConstants.REQUEST_PARAMETER_KEY_ENCODING] =
                    RpcConstants.REQUEST_PARAMETER_VALUE_JSON_PARSED
                this[RpcConstants.REQUEST_PARAMETER_KEY_COMMITMENT] =
                    RpcConstants.REQUEST_PARAMETER_VALUE_CONFIRMED
            }
            val params = listOf(signature, encoding)

            RpcRequest(method = RpcConstants.REQUEST_METHOD_VALUE_GET_CONFIRMED_TRANSACTIONS, params = params)
        }

        val transactions = rpcApi.getConfirmedTransactions(requestsBatch).map { it.result }
        return fromNetworkToDomain(userPublicKey, transactions)
    }

    private fun fromNetworkToDomain(
        tokenPublicKey: String,
        transactions: List<ConfirmedTransactionRootResponse>
    ): List<TransactionDetails> {
        val transactionDetails = mutableListOf<TransactionDetails>()
        transactions.forEachIndexed { index, transaction ->
            try {
                val signature = transaction.transaction?.getTransactionId() ?: return@forEachIndexed
                when {
                    OrcaSwapInstructionParser.isTransactionContainsOrcaSwap(transaction) -> {
                        val orcaSwapDetails =
                            OrcaSwapInstructionParser.parse(signature = signature, transactionRoot = transaction)
                        transactionDetails.add(orcaSwapDetails.getOrThrow())
                    }
                    SerumSwapInstructionParser.isTransactionContainsSerumSwap(transaction) -> {
                        val serumSwapDetails =
                            SerumSwapInstructionParser.parse(signature = signature, transactionRoot = transaction)
                        transactionDetails.add(serumSwapDetails.getOrThrow())
                    }
                    else -> {
                        transactionDetails.addAll(
                            SolanaInstructionParser.parse(
                                signature = signature,
                                transactionRoot = transaction,
                                userInteractor = userInteractor,
                                userPublicKey = tokenPublicKey
                            )
                        )
                    }
                }
                transactionDetails.forEach {
                    it.error = transaction.meta.error?.instructionError
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return transactionDetails
    }
}
