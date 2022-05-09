package org.p2p.wallet.history.interactor.mapper

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
import org.p2p.solanaj.utils.SolanjLogger
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class TransactionDetailsNetworkMapper(
    private val userPublicKey: String,
    private val userInteractor: UserInteractor
) {

    fun fromNetworkToDomain(transactions: List<ConfirmedTransactionRootResponse>): List<TransactionDetails> {
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
                                userPublicKey = userPublicKey
                            )
                        )
                    }
                }
                transactionDetails.forEach {
                    it.error = transaction.meta.error?.instructionError
                }
                val unknownTransactionTypeLogData =
                    "(parsedTransactions=$transactionDetails;\nconfirmedTransaction=${transaction.transaction}"

                SolanjLogger.w(
                    "TransactionDetailsNetworkMapper " +
                        "unknown transactions type, skipping $unknownTransactionTypeLogData"
                )
                SolanjLogger.d(
                    "TransactionDetailsNetworkMapper: " +
                        "Parsing finished: ${transactionDetails.size}; total=${transactions.size}"
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return transactionDetails
    }
}
