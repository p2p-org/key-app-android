package org.p2p.solanaj.kits.transaction

import org.bitcoinj.core.Base58
import org.p2p.solanaj.kits.transaction.ConfirmedTransactionParsed.InstructionParsed
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.solanaj.programs.SerumSwapProgram.serumSwapPID
import org.p2p.solanaj.programs.SystemProgram.SPL_TOKEN_PROGRAM_ID
import org.p2p.solanaj.programs.TokenSwapProgram
import java.math.BigInteger

// todo: Parser should be refactored and optimized
// Get some parsing info from here
// https://github.com/p2p-org/solana-swift/blob/main/Sources/SolanaSwift/Helpers/TransactionParser.swift#L74-L86
object TransactionTypeParser {

    fun parse(transaction: ConfirmedTransactionParsed): List<TransactionDetails> {
        val details: MutableList<TransactionDetails> = ArrayList()
        val signature = transaction.transaction.signatures.firstOrNull().orEmpty()
        val instructions = transaction.transaction.message.instructions

        when {
            getOrcaSwapInstructionIndex(instructions) != -1 ->
                parseDetails(transaction, signature, details)
            getSerumSwapInstructionIndex(instructions) != -1 ->
                parseSerumSwapTransaction(transaction, signature, details)
            instructions.size == 2 ->
                parseCreateAccount(instructions[0], instructions[1])
            instructions.size == 1 ->
                parseCloseAccount()
            instructions.size == 1 || instructions.size == 4 || instructions.size == 2 ->
                parseDetails(transaction, signature, details)
        }

        return details
    }

    private fun parseDetails(
        transaction: ConfirmedTransactionParsed,
        signature: String,
        details: MutableList<TransactionDetails>
    ) {
        for (parsedInstruction in transaction.transaction.message.instructions) {
            val parsedInfo = parsedInstruction.parsed
            if (parsedInfo != null) {
                when (parsedInfo.type) {
                    "transfer",
                    "transferChecked" -> {
                        val transferDetails = TransferDetails(
                            signature,
                            transaction.blockTime,
                            transaction.slot,
                            transaction.meta.fee,
                            parsedInfo.type,
                            parsedInfo.info
                        )
                        details.add(transferDetails)
                    }
                    "closeAccount" -> if (parsedInstruction.programId.equals(SPL_TOKEN_PROGRAM_ID.toBase58())) {
                        val closeDetails = CloseAccountDetails(
                            signature, transaction.blockTime, transaction.slot, parsedInfo.info
                        )
                        details.add(closeDetails)
                    } else {
                        parseSwapDetails(signature, transaction, details, parsedInstruction)
                    }
                    else -> details.add(
                        UnknownDetails(
                            signature,
                            transaction.blockTime,
                            transaction.slot,
                            parsedInfo.info
                        )
                    )
                }
            } else {
                parseSwapDetails(signature, transaction, details, parsedInstruction)
            }
        }
    }

    private fun parseCreateAccount(
        instruction: InstructionParsed,
        initializeAccountInstruction: InstructionParsed
    ) {
        // todo
    }

    private fun parseCloseAccount() {
        // todo
    }

    private fun parseSwapDetails(
        signature: String,
        transaction: ConfirmedTransactionParsed,
        details: MutableList<TransactionDetails>,
        parsedInstruction: InstructionParsed
    ) {
        if (SwapDetails.KNOWN_SWAP_PROGRAM_IDS.contains(parsedInstruction.programId)) {
            val data = Base58.decode(parsedInstruction.data)
            val instructionIndex = data[0].toInt()
            if (instructionIndex == TokenSwapProgram.INSTRUCTION_INDEX_SWAP) {
                val innerInstructions = transaction.meta.innerInstructions
                val instructionParsed: MutableList<InstructionParsed> = ArrayList()
                for (instruction in innerInstructions) {
                    for (ip in instruction.instructions) {
                        if (ip.parsed.type == "transfer") {
                            instructionParsed.add(ip)
                        }
                    }
                }
                if (instructionParsed.isEmpty()) return
                var firstIndex = 0
                var secondIndex = 1
                if (instructionParsed.size > 2) {
                    firstIndex += 1
                    secondIndex += 1
                }
                val amountA = instructionParsed[firstIndex].parsed.info["amount"] as String?
                val userSource = instructionParsed[firstIndex].parsed.info["source"] as String?
                val amountB = instructionParsed[secondIndex].parsed.info["amount"] as String?
                val poolDestination = instructionParsed[secondIndex].parsed.info["source"] as String?
                val destination = instructionParsed[secondIndex].parsed.info["destination"] as String?
                val userSourceKeyIndex = parsedInstruction.accounts.indexOf(userSource)
                val poolDestinationKeyIndex = parsedInstruction.accounts.indexOf(poolDestination)
                val postTokenBalances = transaction.meta.postTokenBalances
                var mintA = ""
                var mintB = ""
                for (balance in postTokenBalances) {
                    if (balance.accountIndex == userSourceKeyIndex) {
                        mintA = balance.mint
                    }
                    if (balance.accountIndex == poolDestinationKeyIndex) {
                        mintB = balance.mint
                    }
                }
                val swapDetails = SwapDetails(
                    signature,
                    transaction.blockTime,
                    transaction.slot,
                    transaction.meta.fee,
                    destination,
                    amountA,
                    amountB,
                    mintA,
                    mintB
                )
                details.add(swapDetails)
            }
        } else {
            details.add(UnknownDetails(signature, transaction.blockTime, transaction.slot, parsedInstruction.data))
        }
    }

    private fun parseSerumSwapTransaction(
        transaction: ConfirmedTransactionParsed,
        signature: String,
        details: MutableList<TransactionDetails>
    ) {
        val instructions = transaction.transaction.message.instructions
        val swapInstructionIndex = getSerumSwapInstructionIndex(instructions)

        val preTokenBalances = transaction.meta.preTokenBalances

        val innerInstruction = transaction.meta.innerInstructions.firstOrNull()

        // get swapInstruction
        val swapInstruction = instructions.getOrNull(swapInstructionIndex) ?: return

        // get all mints
        val mints = preTokenBalances.map { it.mint }.distinct().toMutableList()
        if (mints.size < 2) return

        // transitive swap: remove usdc or usdt if exists
        if (mints.size == 3) {
            mints.removeAll { isUsdx(it) }
        }

        // define swap type
        val isTransitiveSwap = mints.none { isUsdx(it) }

        // assert
        val accounts = swapInstruction.accounts
        if (accounts.isEmpty()) return

        if (isTransitiveSwap && accounts.size != 27) return

        if (!isTransitiveSwap && accounts.size != 16) return

        // get from and to address
        var fromAddress: String
        var toAddress: String

        if (isTransitiveSwap) { // transitive
            fromAddress = accounts[6]
            toAddress = accounts[21]
        } else { // direct
            fromAddress = accounts[10]
            toAddress = accounts[12]

            if (isUsdx(mints.firstOrNull()) && !isUsdx(mints.lastOrNull())) {
                val temp = fromAddress
                fromAddress = toAddress
                toAddress = temp
            }
        }

        // amounts
        val fromInstruction = innerInstruction?.instructions?.find {
            it.parsed?.type == "transfer" && it.parsed.info["source"] == fromAddress
        }

        val amountString = fromInstruction?.parsed?.info?.get("amount") as? String
        val fromAmount = amountString?.let { BigInteger(it) } ?: BigInteger.ZERO

        val toInstruction = innerInstruction?.instructions?.find {
            it.parsed?.type == "transfer" && it.parsed.info["destination"] == toAddress
        }
        val toAmountString = toInstruction?.parsed?.info?.get("amount") as? String
        val toAmount = toAmountString?.let { BigInteger(it) } ?: BigInteger.ZERO

        // if swap from native sol, detect if from or to address is a new account
        val createAccountInstruction = instructions.firstOrNull {
            it.parsed?.type == "createAccount" && it.parsed.info["newAccount"] == fromAddress
        }

        val realSource = createAccountInstruction?.parsed?.info?.get("source") as? String
        if (!realSource.isNullOrEmpty()) {
            fromAddress = realSource
        }

        // get token from mint address and finish request
        val transactionDetails = SwapDetails(
            signature,
            transaction.blockTime,
            transaction.slot,
            transaction.meta.fee,
            toAddress,
            fromAmount.toString(),
            toAmount.toString(),
            mints[0],
            mints[1]
        )

        details.add(transactionDetails)
    }

    // Serum swap
    private fun getSerumSwapInstructionIndex(instructions: List<InstructionParsed>): Int {
        return instructions.indexOfLast { it.programId == serumSwapPID.toBase58() }
    }

    private fun getOrcaSwapInstructionIndex(instructions: List<InstructionParsed>): Int =
        instructions.indexOfFirst {
            SwapDetails.KNOWN_SWAP_PROGRAM_IDS.contains(it.programId)
        }

    private fun isUsdx(value: String?): Boolean {
        return value == SerumSwapProgram.usdcMint.toBase58() || value == SerumSwapProgram.usdtMint.toBase58()
    }
}