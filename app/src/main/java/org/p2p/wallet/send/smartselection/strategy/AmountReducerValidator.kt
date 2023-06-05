package org.p2p.wallet.send.smartselection.strategy

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee

/**
 * If user is not able to cover fees with any SOL or SPL tokens,
 * then we are trying to reduce the amount if input amount
 * */
class AmountReducerValidator {

    fun isValid(
        inputAmount: BigInteger,
        sourceToken: Token.Active,
        fee: FeeRelayerFee,
        feePayerToken: Token.Active
    ): Boolean {
        if (!sourceToken.isSOL) return false

        // assuming that fee payer is the same as source token
        if (sourceToken.mintAddress != feePayerToken.mintAddress) return false

        // calculating required amount in SPL token
        val requiredAmount = fee.totalInSpl + inputAmount

        // checking if fee payer balance is enough to cover fee and input amount
        val tokenTotal = feePayerToken.totalInLamports
        return tokenTotal >= requiredAmount
    }
}
