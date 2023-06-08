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
 * SOL token as a fee payer is a second priority in paying fees after initial tokens
 * */
class SolanaTokenStrategy(
    private val recipient: SearchResult,
    private val solToken: Token.Active,
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee,
    private val minRentExemption: BigInteger
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)

        // calculating if SOL balance is enough
        val totalSolNeeded = if (sourceToken.isSOL) {
            fee.totalInSol + inputAmountLamports
        } else {
            fee.totalInSol
        }

        if (solToken.totalInLamports < totalSolNeeded) {
            return false
        }

        // checking if source token balance is enough to cover input
        if (sourceToken.totalInLamports < inputAmountLamports) {
            return false
        }

        return true
    }

    override fun execute(): FeePayerState {
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
