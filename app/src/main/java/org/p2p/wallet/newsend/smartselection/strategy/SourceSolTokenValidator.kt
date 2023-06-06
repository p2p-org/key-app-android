package org.p2p.wallet.newsend.smartselection.strategy

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

/**
 * Source token should be a highest priority token for fee payment.
 * This validator checks SOL as a source token.
 * */
class SourceSolTokenValidator {

    fun isSolAvailable(
        inputAmount: BigInteger,
        sourceToken: Token.Active,
        fee: FeeRelayerFee
    ): Boolean {
        // checking if source token is SOL
        if (!sourceToken.isSOL) return false

        // if source token is SOL, then we assume that fee is SOL as well
        val requiredAmount = fee.totalInSol + inputAmount

        // checking if SOL balance is enough to cover fee and input amount
        val tokenTotal = sourceToken.totalInLamports
        return tokenTotal >= requiredAmount
    }
}
