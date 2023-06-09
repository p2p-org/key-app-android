package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SearchResult

/**
 * SOL token as a fee payer is a second priority in paying fees after initial tokens
 * This should be called if source token is SPL only
 *
 * We calculate if SOL balance is enough to cover ONLY FEES
 * */
class SolanaTokenStrategy(
    private val recipient: SearchResult,
    private val solToken: Token.Active,
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee,
    private val minRentExemption: BigInteger
) : FeePayerSelectionStrategy {

    // this strategy can be called only if source SPL token balance is not enough to cover fees
    override fun isPayable(): Boolean {
        if (sourceToken.isSOL) {
            return false
        }

        return solToken.totalInLamports >= fee.totalInSol
    }

    override fun execute(): FeePayerState {
        return FeePayerState.CalculationSuccess(
            sourceToken = sourceToken,
            feePayerToken = solToken,
            fee = fee,
            inputAmount = inputAmount
        )
    }
}
