package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState

/**
 * Source token should be a highest priority token for fee payment
 * Checking only if source token total is enough to cover all expenses.
 * */
class SourceTokenStrategy(
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        return if (sourceToken.isSOL) {
            isPayableSol()
        } else {
            isPayableSpl()
        }
    }

    // this should be called only if `isPayable` returned true
    // we are not checking `isPayable` in execute to avoid double work
    override fun execute(): FeePayerState =
        FeePayerState.CalculationSuccess(
            sourceToken = sourceToken,
            feePayerToken = sourceToken,
            fee = fee,
            inputAmount = inputAmount
        )

    private fun isPayableSol(): Boolean {
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)

        // calculating the total needed amount in SOL
        val requiredAmount = fee.totalInSol + inputAmountLamports

        // checking if SOL balance is enough to cover fee and input amount
        val tokenTotal = sourceToken.totalInLamports
        return tokenTotal >= requiredAmount
    }

    private fun isPayableSpl(): Boolean {
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)

        // calculating the total needed amount in SPL
        val requiredAmount = fee.totalInSpl + inputAmountLamports

        // checking if SPL balance is enough to cover fee and input amount
        val tokenTotal = sourceToken.totalInLamports
        return tokenTotal >= requiredAmount
    }
}
