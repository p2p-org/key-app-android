package org.p2p.solanaj.kits.transaction.mapper

import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
import org.p2p.solanaj.utils.SolanjLogger

class TransactionDetailsNetworkMapper {

    private val confirmedTransactionMapper = ConfirmedTransactionRootMapper(
        orcaSwapInstructionParser = OrcaSwapInstructionParser(),
        serumSwapInstructionParser = SerumSwapInstructionParser()
    )

    fun fromNetworkToDomain(
        confirmedTransactionRoots: List<ConfirmedTransactionRootResponse>
    ): List<TransactionDetails> {
        val resultTransactions = mutableListOf<TransactionDetails>()

        confirmedTransactionRoots.forEach { confirmedTransaction ->
            val parsedTransactions = confirmedTransactionMapper.mapToDomain(
                transactionRoot = confirmedTransaction,
                onErrorLogger = { SolanjLogger.w(it) }
            )

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
