package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy

class CreateAccountParsingStrategy : TransactionParsingStrategy {

    override suspend fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {

        return ParsingResult.Transaction.create(
            CreateAccountDetails(
                signature = signature,
                slot = transactionRoot.slot,
                blockTime = transactionRoot.blockTime,
                fee = transactionRoot.meta.fee,
                mint = null
            )
        )
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.CREATE_ACCOUNT
}
