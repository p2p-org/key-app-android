package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState

/**
 * SPL tokens as a fee payer is a third priority in paying fees after SOL token.
 * This should work only in case, if the source token is not the same SPL as a Fee Payer
 * */
class SplTokenStrategy(
    private val sourceToken: Token.Active,
    private val feePayerToken: Token.Active,
    private val inputAmount: BigDecimal?,
    private val fee: FeeRelayerFee,
    private val alternativeTokens: List<Token.Active>
) : FeePayerSelectionStrategy {

    override fun isPayable(): Boolean {
        // we can't pay fee if source token is SOL
        if (sourceToken.isSOL) return false

        // checking if source token is NOT the same as fee payer
        if (sourceToken.mintAddress == feePayerToken.mintAddress) return false

        // checking if we have other tokens except the source token to cover fees

        // we are assuming that [alternativeFeePayerTokens] doesn't contain the source token
//        if (alternativeFeePayerTokens.isNotEmpty()) return false

        // calculating required amount in SPL token
        val requiredAmount = fee.totalInSpl

        // checking if fee payer balance is enough to cover fee
        val tokenTotal = feePayerToken.totalInLamports
        return tokenTotal >= requiredAmount
    }

    override fun execute(): FeePayerState =
        FeePayerState.CalculationSuccess(
            sourceToken = sourceToken,
            feePayerToken = feePayerToken,
            fee = fee,
            inputAmount = inputAmount
        )

//    private fun findFeePayer() {
//        sendInteractor.findAlternativeFeePayerTokens(
//            userTokens = userInteractor.getNonZeroUserTokens(),
//            feePayerToExclude = newFeePayer,
//            transactionFeeInSOL = feeRelayerFee.transactionFeeInSol,
//            accountCreationFeeInSOL = feeRelayerFee.accountCreationFeeInSol
//        )
//    }
}
