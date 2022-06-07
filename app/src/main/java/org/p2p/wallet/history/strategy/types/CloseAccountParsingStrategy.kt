package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy

class CloseAccountParsingStrategy : TransactionParsingStrategy {

    override fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val instructions = transactionRoot.transaction?.message?.instructions.orEmpty()

        val closedTokenPubKey = instructions.firstOrNull()?.parsed?.info?.account

        val preBalances = transactionRoot.meta.preTokenBalances
        val preTokenBalance = preBalances.firstOrNull()?.mint
        if (instruction.programId != SystemProgram.SPL_TOKEN_PROGRAM_ID.toBase58()) {
            return ParsingResult.Error(IllegalArgumentException("Incorrect program ID"))
        }

        return ParsingResult.Transaction.create(
            CloseAccountDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot,
                account = closedTokenPubKey,
                mint = preTokenBalance
            )
        )
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.CLOSE_ACCOUNT
}
