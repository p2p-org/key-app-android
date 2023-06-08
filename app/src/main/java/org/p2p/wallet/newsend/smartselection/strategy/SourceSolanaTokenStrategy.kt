package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason
import org.p2p.wallet.newsend.smartselection.validator.SourceSolValidator

/**
 * Source token should be a highest priority token for fee payment
 * Checking only if source token total is enough to cover all expenses.
 * */
class SourceSolanaTokenStrategy(
    private val recipient: SearchResult,
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee,
    private val minRentExemption: BigInteger
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        if (!sourceToken.isSOL) {
            return false
        }

        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)

        // calculating the total needed amount in SOL
        val requiredAmount = fee.totalInSol + inputAmountLamports

        // checking if SOL balance is enough to cover fee and input amount
        val tokenTotal = sourceToken.totalInLamports
        return tokenTotal >= requiredAmount
    }

    override fun execute(): FeePayerState {
        val solToken = sourceToken
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)

        val solValidator = SourceSolValidator(
            sourceToken = sourceToken,
            recipient = recipient,
            inputAmount = inputAmountLamports,
            minRentExemption = minRentExemption
        )

        return when {
            solValidator.isLowMinBalanceIgnored() -> {
                val reason = FeePayerFailureReason.LowMinBalanceIgnored
                FeePayerState.Failure(reason)
            }
            solValidator.isAmountInvalidForRecipient() -> {
                val minRequiredSolBalance = minRentExemption.fromLamports().scaleLong().toPlainString()
                val reason = FeePayerFailureReason.InvalidAmountForRecipient(minRequiredSolBalance)
                FeePayerState.Failure(reason)
            }
            solValidator.isAmountInvalidForSender() -> {
                val maxSolAmountAllowedLamports = sourceToken.totalInLamports - minRentExemption
                val maxSolAmountAllowed = maxSolAmountAllowedLamports.fromLamports().scaleLong().toPlainString()
                val reason = FeePayerFailureReason.InvalidAmountForSender(maxSolAmountAllowed)
                FeePayerState.Failure(reason)
            }
            else -> {
                FeePayerState.CalculationSuccess(
                    sourceToken = sourceToken,
                    feePayerToken = solToken,
                    fee = fee,
                    inputAmount = inputAmount
                )
            }
        }
    }
}
