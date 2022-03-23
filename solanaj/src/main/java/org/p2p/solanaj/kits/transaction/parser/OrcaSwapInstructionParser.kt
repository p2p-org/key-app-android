package org.p2p.solanaj.kits.transaction.parser

import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InnerInstructionDetailsResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import java.lang.Exception

private const val ZERO_AMOUNT = "0"
internal class OrcaSwapInstructionParser {

    private class ParsedInstructionDetails(
        val rootInstruction: InstructionResponse,
        val innerInstructionDetails: InnerInstructionDetailsResponse?
    )

    private val orcaSwapProgramIds = setOf(
        "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1", // swap ocra,
        "9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP", // swap ocra v2
        "9qvG1zUp8xF1Bi4m6UdRNby1BAAuaDrUxSpv4CmRRMjL", // main deprecated
        "SwaPpA9LAaLfeLi3a68M4DjnLqgtticKg6CnyNwgAC8", // main deprecated
    )

    internal class OrcaSwapTransactionParseError(message: String) : Exception(message)

    fun isTransactionContainsOrcaSwap(transactionRoot: ConfirmedTransactionRootResponse): Boolean {
        val instructions = transactionRoot.transaction?.message?.instructions

        val orcaSwapInstruction = instructions?.firstOrNull { it.programId in orcaSwapProgramIds }
        return orcaSwapInstruction != null
    }

    fun parse(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse
    ): Result<SwapDetails> {
        val innerInstructions = transactionRoot.meta.innerInstructions

        if (isLiquidityToPool(innerInstructions)) {
            return parseResultFailure("($signature) Transaction is Liquid to pool")
        }

        if (isBurn(innerInstructions)) {
            return parseResultFailure("($signature) Transaction is Burn")
        }

        val swapInstructions = transactionRoot.parsedInstructionDetails.filter {
            it.rootInstruction.programId in orcaSwapProgramIds
        }

        if (swapInstructions.isEmpty()) {
            return parseFailedInstruction(signature, transactionRoot)
        }

        val sourceInstruction = swapInstructions.firstOrNull()?.innerInstructionDetails?.instructions?.firstOrNull()
        val destinationInstruction = swapInstructions.lastOrNull()?.innerInstructionDetails?.instructions?.lastOrNull()

        if (sourceInstruction == null || destinationInstruction == null) {
            return parseFailedInstruction(signature, transactionRoot)
        }

        val sourceInfo = sourceInstruction.parsed?.info
        val destinationInfo = destinationInstruction.parsed?.info

        var source = sourceInfo?.source ?: sourceInfo?.destination
        val sourceAmount = sourceInfo?.amount ?: "0"
        var destination = destinationInfo?.destination ?: destinationInfo?.source
        val destinationAmount = destinationInfo?.amount ?: "0"

        // For swap with WSOL. So WSOL account closed after tx and we try to find true account
        // 1. SOL/X
        // 2. X/SOL
        val closeInstruction = transactionRoot.transaction
            ?.message
            ?.instructions
            ?.find { it.parsed?.type == "closeAccount" }

        if (closeInstruction?.parsed != null) {
            if (closeInstruction.parsed.info.account == source) {
                source = closeInstruction.parsed.info.destination
            }
            if (closeInstruction.parsed.info.account == destination) {
                destination = closeInstruction.parsed.info.destination
            }
        }

        return Result.success(
            SwapDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot,
                fee = transactionRoot.meta.fee,
                source = source,
                destination = destination,
                amountA = sourceAmount,
                amountB = destinationAmount,
                mintA = null,
                mintB = null,
                alternateSource = sourceInfo?.destination,
                alternateDestination = destinationInfo?.source,
            )
        )
    }

    private fun parseFailedInstruction(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse,
    ): Result<SwapDetails> {
        val postTokenBalances = transactionRoot.meta.postTokenBalances
            ?: return parseResultFailure("($signature) meta.postTokenBalances is null")

        val sourceTokenBalance = postTokenBalances.firstOrNull()
        val destinationTokenBalance = postTokenBalances.lastOrNull()

        val approveInstruction = transactionRoot.transaction
            ?.message
            ?.instructions
            ?.find { it.parsed?.type == "approve" }
            ?: return parseResultFailure("($signature) transaction.message.instructions doesn't contain 'approve' type")

        val sourceMint = sourceTokenBalance?.mint
            ?: return parseResultFailure("($signature) postTokenBalances[0].mint aka source is null")
        val destinationMint = destinationTokenBalance?.mint
            ?: return parseResultFailure("($signature) postTokenBalances[-1].mint aka destination is null")

        val source = approveInstruction.parsed?.info?.source
        val destination = approveInstruction.parsed
            ?.info
            ?.owner.takeIf { destinationMint == "Ejmc1UB4EsES5oAaRN63SpoxMJidt3ZGBrqrZk49vjTZ" }

        val sourceAmount =
            approveInstruction.parsed?.info?.amount
                ?: sourceTokenBalance.uiTokenAmountDetails?.amount
                ?: ZERO_AMOUNT
        val destinationAmount =
            destinationTokenBalance.uiTokenAmountDetails?.amount
                ?: ZERO_AMOUNT

        return Result.success(
            SwapDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot,
                fee = transactionRoot.meta.fee,
                source = source,
                destination = destination,
                amountA = sourceAmount,
                amountB = destinationAmount,
                mintA = sourceMint,
                mintB = destinationMint,
                alternateSource = null,
                alternateDestination = null
            )
        )
    }

    /**
     * Check liquidity to pool
     */
    private fun isLiquidityToPool(innerInstructions: List<InnerInstructionDetailsResponse>?): Boolean {
        val instructions = innerInstructions?.firstOrNull()?.instructions
        return instructions?.size == 3 &&
            instructions[0].parsed?.type == "transfer" &&
            instructions[1].parsed?.type == "transfer" &&
            instructions[2].parsed?.type == "mintTo"
    }

    /**
     * Check liquidity to pool
     */
    private fun isBurn(innerInstructions: List<InnerInstructionDetailsResponse>?): Boolean {
        val instructions = innerInstructions?.firstOrNull()?.instructions
        return instructions?.size == 3 &&
            instructions[0].parsed?.type == "burn" &&
            instructions[1].parsed?.type == "transfer" &&
            instructions[2].parsed?.type == "transfer"
    }

    private val ConfirmedTransactionRootResponse.parsedInstructionDetails: List<ParsedInstructionDetails>
        get() {
            val rootInstructions = transaction?.message?.instructions.orEmpty()
            val innerInstructions: List<InnerInstructionDetailsResponse> = meta.innerInstructions.orEmpty()

            return rootInstructions.mapIndexed { rootIndex, instructionResponse ->
                ParsedInstructionDetails(
                    rootInstruction = instructionResponse,
                    innerInstructionDetails = innerInstructions.firstOrNull { it.instructionIndex == rootIndex }
                )
            }
        }

    private fun parseResultFailure(message: String): Result<SwapDetails> {
        return Result.failure(OrcaSwapTransactionParseError(message))
    }
}
