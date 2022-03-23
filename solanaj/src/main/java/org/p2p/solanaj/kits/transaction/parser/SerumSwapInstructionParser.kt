package org.p2p.solanaj.kits.transaction.parser

import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.solanaj.programs.SerumSwapProgram
import java.lang.Exception
import java.math.BigInteger

internal class SerumSwapInstructionParser {

    internal class SerumSwapParseError(message: String) : Exception(message)

    fun isTransactionContainsSerumSwap(transactionRoot: ConfirmedTransactionRootResponse): Boolean {
        val instructions = transactionRoot.transaction?.message?.instructions.orEmpty()
        return getSerumSwapInstruction(instructions) != null
    }

    fun parse(
        signature: String,
        transactionRoot: ConfirmedTransactionRootResponse,
    ): Result<SwapDetails> {
        val parsedTransaction = transactionRoot.transaction
            ?: return parseError("($signature) .transaction is null")
        val instructions = parsedTransaction.message.instructions

        val preTokenBalances = transactionRoot.meta.preTokenBalances

        // get swapInstruction
        val swapInstruction = getSerumSwapInstruction(instructions)
            ?: return parseError("($signature) swapInstruction is null")

        // get all mints
        val mints = preTokenBalances.map { it.mint }.distinct().toMutableList()
        if (mints.size < 2) return parseError("($signature) preTokenBalance.mint < 2")

        // transitive swap: remove usdc or usdt if exists
        if (mints.size == 3) {
            mints.removeAll(this::isMintUsdx)
        }

        // define swap type
        val isTransitiveSwap = mints.none(this::isMintUsdx)

        // assert
        val accounts = swapInstruction.accounts
        if (accounts.isEmpty()) return parseError("($signature) swapInstruction.accounts == 0")

        if (isTransitiveSwap && accounts.size != 27) {
            return parseError("($signature) isTransitiveSwap && swapInstruction.accounts.size != 27")
        }

        if (!isTransitiveSwap && accounts.size != 16) {
            return parseError("($signature) isTransitiveSwap && swapInstruction.accounts.size != 16")
        }

        // get from and to address
        var fromAddress: String
        var toAddress: String

        if (isTransitiveSwap) {
            fromAddress = accounts[6]
            toAddress = accounts[21]
        } else { // direct
            fromAddress = accounts[10]
            toAddress = accounts[12]

            if (isMintUsdx(mints.firstOrNull()) && !isMintUsdx(mints.lastOrNull())) {
                val temp = fromAddress
                fromAddress = toAddress
                toAddress = temp
            }
        }

        val innerInstruction = transactionRoot.meta.innerInstructions?.firstOrNull()
        // amounts
        val fromInstruction = innerInstruction?.instructions?.find {
            it.parsed?.type == "transfer" && it.parsed.info.source == fromAddress
        }

        val amountString = fromInstruction?.parsed?.info?.amount
        val fromAmount = amountString?.let { BigInteger(it) } ?: BigInteger.ZERO

        val toInstruction = innerInstruction?.instructions?.find {
            it.parsed?.type == "transfer" && it.parsed.info.destination == toAddress
        }
        val toAmountString = toInstruction?.parsed?.info?.amount
        val toAmount = toAmountString?.let { BigInteger(it) } ?: BigInteger.ZERO

        // if swap from native sol, detect if from or to address is a new account
        val createAccountInstruction = instructions.firstOrNull {
            it.parsed?.type == "createAccount" && it.parsed.info.newAccount == fromAddress
        }

        val realSource = createAccountInstruction?.parsed?.info?.source
        if (!realSource.isNullOrEmpty()) {
            fromAddress = realSource
        }

        // get token from mint address and finish request
        return Result.success(
            SwapDetails(
                signature = signature,
                blockTime = transactionRoot.blockTime,
                slot = transactionRoot.slot,
                fee = transactionRoot.meta.fee,
                source = fromAddress,
                destination = toAddress,
                amountA = fromAmount.toString(),
                amountB = toAmount.toString(),
                mintA = mints[0],
                mintB = mints[1],
                alternateSource = null,
                alternateDestination = null
            )
        )
    }

    // Serum swap
    private fun getSerumSwapInstruction(instructions: List<InstructionResponse>): InstructionResponse? {
        return instructions.lastOrNull { it.programId == SerumSwapProgram.serumSwapPID.toBase58() }
    }

    private fun isMintUsdx(mintValue: String?): Boolean {
        return mintValue == SerumSwapProgram.usdcMint.toBase58() || mintValue == SerumSwapProgram.usdtMint.toBase58()
    }

    private fun parseError(message: String): Result<SwapDetails> {
        return Result.failure(SerumSwapParseError(message))
    }
}
