package org.p2p.wallet.history.interactor.mapper

import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
import org.p2p.solanaj.utils.SolanjLogger

class TransactionDetailsNetworkMapper(
    val userPublicKey: String
) {

    private val confirmedTransactionMapper = ConfirmedTransactionRootMapper(
        orcaSwapInstructionParser = OrcaSwapInstructionParser,
        serumSwapInstructionParser = SerumSwapInstructionParser,
        userPublicKey = userPublicKey
    )

    fun fromNetworkToDomain(transactions: List<ConfirmedTransactionRootResponse>): List<TransactionDetails> {
        transactions.forEachIndexed { index, transaction ->
            val signature = transaction.transaction?.getTransactionId() ?: return@forEachIndexed
            when {
                OrcaSwapInstructionParser.isTransactionContainsOrcaSwap(transaction) -> {
                    OrcaSwapInstructionParser.parse(signature = signature, transactionRoot = transaction)
                }
                SerumSwapInstructionParser.isTransactionContainsSerumSwap(transaction) -> {
                    SerumSwapInstructionParser.parse(signature = signature, transactionRoot = transaction)
                }
                else -> {
                    parseTransaction(signature, transaction)
                }
            }
        }
    }

    private fun parseTransaction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse
    ): TransactionDetails {
        transactionRoot.transaction?.message?.instructions?.mapNotNull { instruction ->
            parseInstructionByType(
                signature = signature,
                transactionRoot = transactionRoot,
                parsedInstruction = instruction
            )
        }
    }

    private fun parseInstructionByType(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse,
        parsedInstruction: InstructionResponse
    ): TransactionDetails? {
        val parsedInfo = parsedInstruction.parsed
        return when (parsedInfo?.type) {
            "burnChecked" -> {
                parseBurnOrMintTransaction(
                    signature = signature,
                    transactionRoot = transactionRoot,
                    parsedInfo = parsedInfo
                )
            }
            "transfer", "transferChecked" -> {
                parseTransferTransaction(
                    signature = signature,
                    transactionRoot = transactionRoot,
                    parsedInfo = parsedInfo
                )
            }
            "closeAccount" -> {
                parseCloseTransaction(
                    parsedInstruction = parsedInstruction,
                    parsedInfo = parsedInfo,
                    transactionRoot = transactionRoot,
                    signature = signature,
                )
            }
            "create" -> {
                parseCreateAccountTransaction(
                    signature = signature,
                    transactionRoot = transactionRoot
                )
            }
            else -> {
                parseUnknownTransaction(
                    signature = signature,
                    transactionRoot = transactionRoot
                )
            }
        }
    }

    fun fromNetworkToDomain(
        confirmedTransactionRoots: List<ConfirmedTransactionRootResponse>,
        findMintAddress: (String) -> String
    ): List<TransactionDetails> {

        val resultTransactions = mutableListOf<TransactionDetails>()

        confirmedTransactionRoots.forEach { confirmedTransaction ->
            val parsedTransactions = confirmedTransactionMapper.mapToDomain(
                transactionRoot = confirmedTransaction,
                onErrorLogger = { SolanjLogger.w(it) },
                findMintAddress = findMintAddress
            )
            parsedTransactions.forEach {
                it.error = confirmedTransaction.meta.error?.instructionError
            }
            val swapTransaction = parsedTransactions.firstOrNull { it is SwapDetails }
            if (swapTransaction != null) {
                resultTransactions.add(swapTransaction)
                return@forEach
            }

            val burnOrMintTransaction = parsedTransactions.firstOrNull { it is BurnOrMintDetails }
            if (burnOrMintTransaction != null) {
                resultTransactions.add(burnOrMintTransaction)
                return@forEach
            }

            val transferTransaction = parsedTransactions.firstOrNull { it is TransferDetails }
            if (transferTransaction != null) {
                resultTransactions.add(transferTransaction)
                return@forEach
            }

            val createTransaction = parsedTransactions.firstOrNull { it is CreateAccountDetails }
            if (createTransaction != null) {
                resultTransactions.add(createTransaction)
                return@forEach
            }

            val closeTransaction = parsedTransactions.firstOrNull { it is CloseAccountDetails }
            if (closeTransaction != null) {
                resultTransactions.add(closeTransaction)
                return@forEach
            }

            val unknownTransaction = parsedTransactions.firstOrNull { it is UnknownDetails }
            if (unknownTransaction != null) {
                resultTransactions.add(unknownTransaction)
                return@forEach
            }

            val unknownTransactionTypeLogData =
                "(parsedTransactions=$parsedTransactions;\nconfirmedTransaction=${confirmedTransaction.transaction}"
            SolanjLogger.w(
                "TransactionDetailsNetworkMapper " +
                    "unknown transactions type, skipping $unknownTransactionTypeLogData"
            )
        }

        SolanjLogger.d(
            "TransactionDetailsNetworkMapper: " +
                "Parsing finished: ${resultTransactions.size}; total=${confirmedTransactionRoots.size}"
        )
        return resultTransactions.toList()
    }
}
