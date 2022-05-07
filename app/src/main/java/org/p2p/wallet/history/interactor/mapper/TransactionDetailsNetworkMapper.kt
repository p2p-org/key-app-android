package org.p2p.wallet.history.interactor.mapper

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
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
                            TransactionDetailsParser.parse(
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
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return transactionDetails
    }

//    fun fromNetworkToDomain(
//        confirmedTransactionRoots: List<ConfirmedTransactionRootResponse>,
//        findMintAddress: (String) -> String
//    ): List<TransactionDetails> {
//
//        val resultTransactions = mutableListOf<TransactionDetails>()
//
//        confirmedTransactionRoots.forEach { confirmedTransaction ->
//            val parsedTransactions = mapToDomain(
//                transactionRoot = confirmedTransaction,
//                onErrorLogger = { SolanjLogger.w(it) },
//                findMintAddress = findMintAddress
//            )
//
//            parsedTransactions.forEach {
//                it.error = confirmedTransaction.meta.error?.instructionError
//            }
//
//            val swapTransaction = parsedTransactions.firstOrNull { it is SwapDetails }
//            if (swapTransaction != null) {
//                resultTransactions.add(swapTransaction)
//                return@forEach
//            }
//
//            val burnOrMintTransaction = parsedTransactions.firstOrNull { it is BurnOrMintDetails }
//            if (burnOrMintTransaction != null) {
//                resultTransactions.add(burnOrMintTransaction)
//                return@forEach
//            }
//
//            val transferTransaction = parsedTransactions.firstOrNull { it is TransferDetails }
//            if (transferTransaction != null) {
//                resultTransactions.add(transferTransaction)
//                return@forEach
//            }
//
//            val createTransaction = parsedTransactions.firstOrNull { it is CreateAccountDetails }
//            if (createTransaction != null) {
//                resultTransactions.add(createTransaction)
//                return@forEach
//            }
//
//            val closeTransaction = parsedTransactions.firstOrNull { it is CloseAccountDetails }
//            if (closeTransaction != null) {
//                resultTransactions.add(closeTransaction)
//                return@forEach
//            }
//
//            val unknownTransaction = parsedTransactions.firstOrNull { it is UnknownDetails }
//            if (unknownTransaction != null) {
//                resultTransactions.add(unknownTransaction)
//                return@forEach
//            }
//
//            val unknownTransactionTypeLogData =
//                "(parsedTransactions=$parsedTransactions;\nconfirmedTransaction=${confirmedTransaction.transaction}"
//            SolanjLogger.w(
//                "TransactionDetailsNetworkMapper " +
//                    "unknown transactions type, skipping $unknownTransactionTypeLogData"
//            )
//        }
//
//        SolanjLogger.d(
//            "TransactionDetailsNetworkMapper: " +
//                "Parsing finished: ${resultTransactions.size}; total=${confirmedTransactionRoots.size}"
//        )
//        return resultTransactions.toList()
//    }
}
