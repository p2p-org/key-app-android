package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import java.lang.IllegalStateException

class SerumSwapParsingContext(
    private val strategy: TransactionParsingStrategy
) : TransactionParsingContext {

    override fun parseTransaction(
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val instructions =
            transactionRoot.transaction?.message?.instructions?.filter { it.parsed != null }?.map { instruction ->
                val signature = transactionRoot.transaction?.getTransactionId()
                    ?: return ParsingResult.Error(IllegalStateException("Signature cannot be null"))

                strategy.parseTransaction(
                    signature = signature,
                    instruction = instruction,
                    transactionRoot = transactionRoot
                )
            }
        val details = instructions?.filterIsInstance<ParsingResult.Transaction>()?.flatMap { it.items } ?: emptyList()

        return ParsingResult.Transaction(details)
    }

    override fun canParse(transactionRoot: ConfirmedTransactionRootResponse): Boolean {
        val instructions = transactionRoot.transaction?.message?.instructions.orEmpty()
        return getSerumSwapInstruction(instructions) != null
    }

    private fun getSerumSwapInstruction(instructions: List<InstructionResponse>): InstructionResponse? {
        return instructions.lastOrNull { it.programId == SerumSwapProgram.serumSwapPID.toBase58() }
    }
}
