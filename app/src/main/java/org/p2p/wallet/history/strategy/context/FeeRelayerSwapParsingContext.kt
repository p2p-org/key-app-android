package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.core.utils.Constants
import org.p2p.core.utils.orZero
import java.math.BigInteger

class FeeRelayerSwapParsingContext : TransactionParsingContext {

    private val orcaSwapProgramIds = listOf("12YKFL4mnZz6CBEGePrf293mEzueQM3h8VLPUJsKpGs9")

    override fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean {
        val instructions = transaction.transaction?.message?.instructions
        val orcaSwapInstruction = instructions?.firstOrNull { it.programId in orcaSwapProgramIds }
        return orcaSwapInstruction != null
    }

    override suspend fun parseTransaction(root: ConfirmedTransactionRootResponse): ParsingResult {
        val instructions = root.transaction?.message?.instructions.orEmpty()

        val swapInstruction = instructions.firstOrNull {
            val isP2PSwapProgram = it.programId in orcaSwapProgramIds
            val isFirstByteEqualsToFour = it.data?.let { it1 -> Base58Utils.decode(it1).first().toInt() } == 4
            isP2PSwapProgram && isFirstByteEqualsToFour
        }
        val swapInstructionIndex = if (swapInstruction != null) {
            instructions.indexOf(swapInstruction)
        } else {
            null
        } ?: return ParsingResult.Error(Throwable("It's not parcelable transaction"))

        val extractedSwapInstruction = instructions[swapInstructionIndex]

        val sourceAddress = extractedSwapInstruction.accounts[3]
        var sourceMintAndAmount = parseToken(root, sourceAddress)

        val destinationAddress = extractedSwapInstruction.accounts[5]
        var destinationMintAndAmount = parseToken(root, destinationAddress)

        val instructionsCount = root.transaction?.message?.instructions?.count().orZero()

        if (sourceMintAndAmount.second == "0" && swapInstructionIndex + 1 < instructionsCount) {
            val closeInstruction = root.transaction
                ?.message
                ?.instructions
                ?.getOrNull(swapInstructionIndex + 1)

            if (closeInstruction?.parsed?.type == "closeAccount") {
                closeInstruction
                    .parsed
                    ?.info
                    ?.destination
                    ?.let {
                        sourceMintAndAmount = parseToken(root, it)
                    }
            }
        }

        if (destinationMintAndAmount.second == "0" && swapInstructionIndex + 1 < instructionsCount) {
            val closeInstruction = root.transaction
                ?.message
                ?.instructions
                ?.getOrNull(swapInstructionIndex + 1)

            if (closeInstruction?.parsed?.type == "closeAccount") {
                closeInstruction
                    .parsed
                    ?.info
                    ?.destination
                    ?.let {
                        destinationMintAndAmount = parseToken(root, it)
                    }
            }
        }

        return ParsingResult.Transaction.create(
            SwapDetails(
                signature = root.transaction?.getTransactionId().orEmpty(),
                blockTime = root.blockTime,
                slot = root.slot,
                fee = root.meta.fee,
                source = sourceAddress,
                destination = destinationAddress,
                amountA = sourceMintAndAmount.second,
                amountB = destinationMintAndAmount.second,
                mintA = sourceMintAndAmount.first.address,
                mintB = destinationMintAndAmount.first.address,
                alternateSource = extractedSwapInstruction.parsed?.info?.source,
                alternateDestination = extractedSwapInstruction.parsed?.info?.destination,
            )
        )
    }

    private fun parseToken(root: ConfirmedTransactionRootResponse, address: String): Pair<WalletAmount, String> {

        var addressIndex = root.transaction?.message?.accountKeys?.indexOfFirst {
            address == it.publicKey
        }
        addressIndex = if (addressIndex == -1) null else addressIndex

        val mintAddress = root.meta.postTokenBalances?.firstOrNull {
            it.accountIndex == addressIndex
        }?.mint ?: Constants.WRAPPED_SOL_MINT

        val preBalance: String
        val postBalance: String
        var preBalanceWallet: String = if (mintAddress == Constants.WRAPPED_SOL_MINT) {
            addressIndex?.let {
                root.meta
                    .preBalances?.get(it)
            } ?: Constants.ZERO_AMOUNT
        } else {
            addressIndex?.let {
                root.meta
                    .preTokenBalances
                    ?.get(it)
                    ?.uiTokenAmountDetails
                    ?.amount
            } ?: Constants.ZERO_AMOUNT
        }

        if (mintAddress == Constants.WRAPPED_SOL_MINT) {
            preBalance = addressIndex?.let {
                root.meta
                    .preBalances
                    ?.get(it)
            } ?: Constants.ZERO_AMOUNT
            postBalance = addressIndex?.let {
                root.meta
                    .postBalances
                    ?.get(it)
            } ?: Constants.ZERO_AMOUNT
        } else {
            preBalance = root.meta.preTokenBalances
                ?.firstOrNull { it.accountIndex == addressIndex }
                ?.uiTokenAmountDetails
                ?.amount
                ?: Constants.ZERO_AMOUNT
            postBalance = root.meta.postTokenBalances
                ?.firstOrNull { it.accountIndex == addressIndex }
                ?.uiTokenAmountDetails
                ?.amount
                ?: Constants.ZERO_AMOUNT
        }

        val preBalanceInBigInteger = BigInteger(preBalance)
        val postBalanceInBigInteger = BigInteger(postBalance)
        val amount = if (preBalanceInBigInteger > postBalanceInBigInteger) {
            preBalanceInBigInteger - postBalanceInBigInteger
        } else {
            postBalanceInBigInteger - preBalanceInBigInteger
        }
        return WalletAmount(mintAddress, preBalanceWallet) to amount.toString()
    }
}

data class WalletAmount(val address: String, val amount: String)
