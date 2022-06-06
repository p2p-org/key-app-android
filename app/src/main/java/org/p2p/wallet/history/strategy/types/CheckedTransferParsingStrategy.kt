package org.p2p.wallet.history.strategy.types

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import java.lang.IllegalStateException

class CheckedTransferParsingStrategy : TransactionParsingStrategy {

    override fun parseTransaction(
        signature: String,
        instruction: InstructionResponse,
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {

        val instructions = transactionRoot.transaction?.message?.instructions
        val instruction = instructions?.lastOrNull()

        val parsedInfo = instruction?.parsed
        val sourcePubKey = parsedInfo?.info?.source
        val destinationPubKey = parsedInfo?.info?.destination
        val authority = parsedInfo?.info?.authority
        val instructionInfo = parsedInfo?.info
        val lamports: String = instructionInfo?.lamports?.toLong()?.toBigInteger()
            ?.toString() ?: instructionInfo?.amount ?: instructionInfo?.tokenAmount?.amount ?: "0"
        val mint = parsedInfo?.info?.mint
        val decimals = instructionInfo?.tokenAmount?.decimals?.toInt() ?: 0

        if (instruction?.programId == SystemProgram.PROGRAM_ID.toBase58()) {

            return ParsingResult.Transaction.create(
                TransferDetails(
                    signature = signature,
                    blockTime = transactionRoot.blockTime,
                    slot = transactionRoot.slot,
                    fee = transactionRoot.meta.fee,
                    source = sourcePubKey,
                    destination = destinationPubKey,
                    authority = authority,
                    mint = mint,
                    amount = lamports,
                    _decimals = decimals,
                    programId = instruction.programId.orEmpty(),
                    typeStr = parsedInfo?.type
                )
            )
        } else {
            return ParsingResult.Error(IllegalStateException("Not implemented yet"))
        }
    }

    override fun getType(): TransactionDetailsType = TransactionDetailsType.TRANSFER_CHECKED
}
