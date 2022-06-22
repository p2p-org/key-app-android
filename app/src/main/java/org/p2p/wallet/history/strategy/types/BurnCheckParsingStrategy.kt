package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy

class BurnCheckParsingStrategy : TransactionParsingStrategy {

    override suspend fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {

        val parsedInfo = instruction.parsed
        val info = parsedInfo?.info

        return ParsingResult.Transaction.create(
            BurnOrMintDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot,
                fee = transactionRoot.meta.fee,
                account = info?.account,
                authority = info?.authority,
                uiAmount = info?.tokenAmount?.uiAmountString,
                _decimals = info?.tokenAmount?.decimals?.toInt() ?: 0
            )
        )
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.BURN_CHECKED
}
