package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState

/**
 * SOL token as a fee payer is a second priority in paying fees after initial tokens
 * */
class SolanaTokenStrategy(
    private val solToken: Token.Active,
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)
        // calculating required amount in SOL
        val totalNeeded = if (sourceToken.isSOL) {
            fee.totalInSol + inputAmountLamports
        } else {
            fee.totalInSol
        }

        return solToken.totalInLamports >= totalNeeded
    }

    override fun execute(): FeePayerState =
        FeePayerState.CalculationSuccess(
            sourceToken = sourceToken,
            feePayerToken = solToken,
            fee = fee,
            inputAmount = inputAmount
        )
}
