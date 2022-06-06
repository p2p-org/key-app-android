package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import java.lang.IllegalStateException

class TransferParsingStrategy : TransactionParsingStrategy {

    override fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {

        val parsedInfo = instruction.parsed
        if (instruction.programId == SystemProgram.PROGRAM_ID.toBase58()) {
        }
        return ParsingResult.Error(IllegalStateException("Parsing not implemented"))
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.TRANSFER
}
