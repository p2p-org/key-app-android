package org.p2p.solanaj.kits.transaction.mapper

import org.p2p.solanaj.kits.transaction.BurnOrMintDetails
import org.p2p.solanaj.kits.transaction.CloseAccountDetails
import org.p2p.solanaj.kits.transaction.CreateAccountDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.kits.transaction.TransferDetails
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionInfoDetailsResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.kits.transaction.parser.OrcaSwapInstructionParser
import org.p2p.solanaj.kits.transaction.parser.SerumSwapInstructionParser
import org.p2p.solanaj.programs.SystemProgram.SPL_TOKEN_PROGRAM_ID

// Get some parsing info from here
// https://github.com/p2p-org/solana-swift/blob/main/Sources/SolanaSwift/Helpers/TransactionParser.swift#L74-L86
internal class ConfirmedTransactionRootMapper(
    private val orcaSwapInstructionParser: OrcaSwapInstructionParser,
    private val serumSwapInstructionParser: SerumSwapInstructionParser
) {

    fun mapToDomain(
        transactionRoot: ConfirmedTransactionRootResponse,
        onErrorLogger: (Throwable) -> Unit
    ): List<TransactionDetails> {
        val signature = transactionRoot.transaction?.getTransactionId() ?: return emptyList()

        return when {
            orcaSwapInstructionParser.isTransactionContainsOrcaSwap(transactionRoot) -> {
                listOfNotNull(
                    orcaSwapInstructionParser.parse(signature, transactionRoot)
                        .onFailure(onErrorLogger)
                        .getOrNull()
                )
            }
            serumSwapInstructionParser.isTransactionContainsSerumSwap(transactionRoot) -> {
                listOfNotNull(
                    serumSwapInstructionParser.parse(signature, transactionRoot)
                        .onFailure(onErrorLogger)
                        .getOrNull()
                )
            }
            else -> {
                parseTransaction(transactionRoot, signature)
            }
        }
    }

    private fun parseTransaction(
        transactionRoot: ConfirmedTransactionRootResponse,
        signature: String,
    ): List<TransactionDetails> {
        return transactionRoot.transaction?.message
            ?.instructions
            ?.mapNotNull { parseInstructionByType(signature, transactionRoot, it) }
            ?.toMutableList()
            .orEmpty()
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
                    parsedInfo = parsedInfo,
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

    private fun parseCreateAccountTransaction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse
    ): CreateAccountDetails {
        return CreateAccountDetails(
            signature = signature,
            slot = transactionRoot.slot,
            blockTime = transactionRoot.blockTime,
            fee = transactionRoot.meta.fee
        )
    }

    private fun parseUnknownTransaction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse
    ): UnknownDetails {
        return UnknownDetails(
            signature = signature,
            blockTime = transactionRoot.blockTime,
            slot = transactionRoot.slot
        )
    }

    private fun parseCloseTransaction(
        parsedInstruction: InstructionResponse,
        parsedInfo: InstructionInfoDetailsResponse,
        transactionRoot: ConfirmedTransactionRootResponse,
        signature: String,
    ): CloseAccountDetails? {
        if (parsedInstruction.programId != SPL_TOKEN_PROGRAM_ID.toBase58()) {
            return null
        }

        val closedTokenPublicKey = parsedInfo.info.account
        val preBalances = transactionRoot.meta.preTokenBalances.firstOrNull()?.mint
        return CloseAccountDetails(
            signature = signature,
            blockTime = transactionRoot.blockTime,
            slot = transactionRoot.slot,
            account = closedTokenPublicKey,
            mint = preBalances
        )
    }

    private fun parseTransferTransaction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse,
        parsedInfo: InstructionInfoDetailsResponse,
    ): TransferDetails {

        val instruction = transactionRoot.transaction?.message?.instructions?.lastOrNull()

        val sourcePubKey = parsedInfo.info.source
        val destinationPubKey = parsedInfo.info.destination
        val authority = parsedInfo.info.authority
        val instructionInfo = parsedInfo.info
        val lamports: String = instructionInfo.lamports?.toLong()?.toBigInteger()
            ?.toString() ?: instructionInfo.amount ?: instructionInfo.tokenAmount?.amount ?: "0"
        val mint = parsedInfo.info.mint
        val decimals = instructionInfo.tokenAmount?.decimals?.toInt() ?: 0

        return TransferDetails(
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
            programId = instruction?.programId.orEmpty(),
            typeStr = parsedInfo.type
        )
    }

    private fun parseBurnOrMintTransaction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse,
        parsedInfo: InstructionInfoDetailsResponse
    ): BurnOrMintDetails {
        val info = parsedInfo.info

        return BurnOrMintDetails(
            signature = signature,
            blockTime = transactionRoot.blockTime,
            slot = transactionRoot.slot,
            fee = transactionRoot.meta.fee,
            account = info.account,
            authority = info.authority,
            uiAmount = info.tokenAmount?.uiAmountString,
            _decimals = info.tokenAmount?.decimals?.toInt() ?: 0
        )
    }
}
