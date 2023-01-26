package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import timber.log.Timber

class UnknownParsingStrategy : TransactionParsingStrategy {

    override suspend fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val logParams = "type=${instruction.parsed?.type};programId=${instruction.programId}"
        Timber.tag("UnknownParsingStrategy").i("Creating unknown transaction: $logParams")
        return ParsingResult.Transaction.create(
            UnknownDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot
            )
        )
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.UNKNOWN
}
