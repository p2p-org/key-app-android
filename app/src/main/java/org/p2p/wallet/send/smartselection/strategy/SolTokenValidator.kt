package org.p2p.wallet.send.smartselection.strategy

import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

/**
 * SOL token as a fee payer is a second priority in paying fees after initial tokens
 * */
class SolTokenValidator {

    fun isSolAvailable(
        sourceToken: Token.Active,
        fee: FeeRelayerFee,
        feePayerToken: Token.Active
    ): Boolean {
        // validating that source token is SPL only
        if (sourceToken.isSOL) return false

        // validating that fee payer is SOL
        if (!feePayerToken.isSOL) return false

        // calculating required amount in SOL
        val requiredAmount = fee.totalInSol

        return feePayerToken.totalInLamports >= requiredAmount
    }
}
