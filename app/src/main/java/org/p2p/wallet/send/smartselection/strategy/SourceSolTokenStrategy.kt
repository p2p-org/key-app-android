package org.p2p.wallet.send.smartselection.strategy

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.send.model.FeePayerState

/**
 * Source token should be a highest priority token for fee payment
 * Checking only if source token total is enough to cover all expenses.
 * */
class SourceSolTokenStrategy(
    private val sourceToken: Token.Active,
    private val inputAmount: BigInteger,
    private val fee: FeeRelayerFee
) : FeePayerSelectionStrategy {

    override fun execute(): FeePayerState {
        // checking if source token is SOL
        if (!sourceToken.isSOL) return FeePayerState.NotApplicable

        // if source token is SOL, then we assume that fee is SOL as well
        val requiredAmount = fee.totalInSol + inputAmount

        // checking if SOL balance is enough to cover fee and input amount
        val tokenTotal = sourceToken.totalInLamports
        val isPayable = tokenTotal >= requiredAmount

        if (!isPayable) {
            return FeePayerState.NotApplicable
        }

        return FeePayerState.FeePayerFound(feePayerToken = sourceToken, fee = fee)
    }
}
