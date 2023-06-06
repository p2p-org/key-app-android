package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SendFatalError
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason

class ValidationStrategy(
    private val sourceToken: Token.Active,
    private val feePayerToken: Token.Active,
    private val searchResult: SearchResult,
    private val minRentExemption: BigInteger,
    private val inputAmount: BigDecimal,
    private val fee: FeeRelayerFee
) : FeePayerSelectionStrategy {

    private val inputAmountLamports = inputAmount.toLamports(sourceToken.decimals)

    override fun isPayable(): Boolean = true

    override fun execute(): FeePayerState = when {
        !isEnoughBalance() -> {
            val reason = FeePayerFailureReason.InputExceeded(sourceToken)
            FeePayerState.Failure(reason)
        }
        !isEnoughToCoverExpenses() -> {
            val reason = FeePayerFailureReason.ExceededFee
            FeePayerState.Failure(reason)
        }
        !isMinRequiredBalanceLeft() -> {
            val reason = FeePayerFailureReason.LowMinBalanceIgnored
            FeePayerState.Failure(reason)
        }
        !isAmountValidForRecipient() -> {
            val minRequiredSolBalance = minRentExemption.fromLamports().scaleLong().toPlainString()
            val reason = FeePayerFailureReason.InvalidAmountForRecipient(minRequiredSolBalance)
            FeePayerState.Failure(reason)
        }
        !isAmountValidForSender() -> {
            val maxSolAmountAllowedLamports = sourceToken.totalInLamports - minRentExemption
            val maxSolAmountAllowed = maxSolAmountAllowedLamports.fromLamports().scaleLong().toPlainString()
            val reason = FeePayerFailureReason.InvalidAmountForSender(maxSolAmountAllowed)
            FeePayerState.Failure(reason)
        }
        else -> {
            val error = SendFatalError("Unknown error during validation")
            val reason = FeePayerFailureReason.CalculationError(error)
            FeePayerState.Failure(reason)
        }
    }

    private fun isEnoughBalance(): Boolean {
        val totalNeeded = inputAmount.toLamports(sourceToken.decimals)
        val totalInLamports = sourceToken.totalInLamports
        return totalNeeded.isLessThan(totalInLamports)
    }

    private fun isEnoughToCoverExpenses(): Boolean = when {
        // if source is SOL, then fee payer is SOL as well
        sourceToken.isSOL -> {
            val totalSolanaAvailable = sourceToken.totalInLamports - minRentExemption
            totalSolanaAvailable >= inputAmountLamports + fee.totalInSol
        }

        // assuming that source token is not SOL
        feePayerToken.isSOL -> {
            val sourceTokenTotal = sourceToken.totalInLamports
            val isEnoughToCoverInput = sourceTokenTotal >= inputAmountLamports
            val totalFeePayerAvailable = feePayerToken.totalInLamports - minRentExemption
            val isEnoughToCoverFees = totalFeePayerAvailable >= fee.totalInSol

            isEnoughToCoverInput && isEnoughToCoverFees
        }
        // assuming that source token and fee payer are same
        sourceToken.tokenSymbol == feePayerToken.tokenSymbol -> {
            val sourceTokenTotal = sourceToken.totalInLamports
            sourceTokenTotal >= inputAmountLamports + fee.totalInSpl
        }
        // assuming that source token and fee payer are different
        else -> {
            feePayerToken.totalInLamports >= fee.totalInSpl
        }
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient should receive at least [minRentExemption] SOL balance if his current balance is 0
     * 2. The recipient should have at least [minRentExemption] after the transaction
     * */
    private fun isAmountValidForRecipient(): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        val isRecipientEmpty = searchResult is SearchResult.AddressFound && searchResult.isEmptyBalance

        val inputAmountLamports = inputAmount.toLamports(sourceToken.decimals)
        val isInputValidForRecipient = inputAmountLamports >= minRentExemption
        if (!isSourceTokenSol) return true

        val isInvalid = isRecipientEmpty && !isInputValidForRecipient
        return !isInvalid
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The sender is allowed to sent exactly the whole balance.
     * 2. It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    private fun isAmountValidForSender(): Boolean {
        val isSourceTokenSol = sourceToken.isSOL
        if (!isSourceTokenSol) return true
        val balanceDiff = sourceToken.totalInLamports - inputAmountLamports

        return balanceDiff.isZero() || balanceDiff >= minRentExemption
    }

    /**
     * Validating only SOL -> SOL operations here
     * The empty recipient is required
     * Checking if the sender should leave at least [minRentExemption] or Zero SOL balance
     * */
    private fun isMinRequiredBalanceLeft(): Boolean {
        if (!sourceToken.isSOL) return true

        val isRecipientEmpty = searchResult is SearchResult.AddressFound && searchResult.isEmptyBalance

        val sourceTotalLamports = sourceToken.totalInLamports
        val minRequiredBalance = minRentExemption

        val inputAmountInLamports = inputAmount.toLamports(sourceToken.decimals)
        val diff = sourceTotalLamports - inputAmountInLamports

        val maxSolAmountAllowed = if (isRecipientEmpty) {
            // if recipient has no solana account (balance == 0) we can send at least minRentExemption amount
            sourceTotalLamports - minRequiredBalance
        } else {
            sourceTotalLamports
        }

        return diff.isZero() || maxSolAmountAllowed >= minRequiredBalance
    }
}
