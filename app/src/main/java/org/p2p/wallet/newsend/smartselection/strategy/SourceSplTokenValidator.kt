package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

/**
 * Source token should be a highest priority token for fee payment
 * Checking only if source token total is enough to cover all expenses. We are not checking other SPL tokens
 * */
class SourceSplTokenValidator {

    fun isSplAvailable(
        inputAmount: BigInteger,
        sourceToken: Token.Active,
        fee: FeeRelayerFee,
        feePayerToken: Token.Active
    ): Boolean {
        // we can't pay fee if source token is SOL
        if (sourceToken.isSOL) return false

        // assuming that fee payer is the same as source token
        if (sourceToken.mintAddress != feePayerToken.mintAddress) return false

        // calculating required amount in SPL token
        val requiredAmount = fee.totalInSpl + inputAmount

        // checking if fee payer balance is enough to cover fee and input amount
        val tokenTotal = feePayerToken.totalInLamports
        return tokenTotal >= requiredAmount
    }
}
