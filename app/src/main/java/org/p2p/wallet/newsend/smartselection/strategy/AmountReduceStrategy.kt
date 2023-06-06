package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState

/**
 * If user is not able to cover fees with any SOL or SPL tokens,
 * then we are trying to reduce the amount if input amount only for SPL tokens
 * */
class AmountReduceStrategy(
    private val sourceToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee,
    private val feePayerToken: Token.Active
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        if (sourceToken.isSOL) {
            return false
        }

        // assuming that fee payer is the same as source token
        if (sourceToken.mintAddress != feePayerToken.mintAddress) {
            return false
        }

        // calculating required amount in SPL token
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)
        val totalNeeded = fee.totalInSpl + inputAmountLamports

        // finding the max available amount we can send
        val sourceTokenTotal = sourceToken.totalInLamports

        // checking if total needed is more than token total
        // making sure that we need to reduce amount
        return sourceTokenTotal.isLessThan(totalNeeded)
    }

    override fun execute(): FeePayerState {
        val inputAmountLamports = inputAmount.orZero().toLamports(sourceToken.decimals)
        val totalNeeded = fee.totalInSpl + inputAmountLamports
        val sourceTokenTotal = sourceToken.totalInLamports

        val diff = totalNeeded - sourceTokenTotal
        val desiredAmount = if (diff.isLessThan(inputAmountLamports)) inputAmountLamports - diff else null
        if (desiredAmount != null) {
            val newInputAmount = desiredAmount.fromLamports(sourceToken.decimals)
            return FeePayerState.ReduceAmount(
                sourceToken = sourceToken,
                feePayerToken = feePayerToken,
                fee = fee,
                newInputAmount = newInputAmount
            )
        }

        return FeePayerState.NoStrategiesFound
    }
}
