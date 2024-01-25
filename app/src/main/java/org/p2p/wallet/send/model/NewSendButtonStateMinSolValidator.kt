package org.p2p.wallet.send.model

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero

/**
 * One place to keep all the logic regarding min SOL validation
 */
class NewSendButtonStateMinSolValidator(
    private val tokenToSend: Token.Active,
    private val minRentExemption: BigInteger,
    private val recipient: SearchResult
) {

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient should receive at least [minRentExemption] SOL balance if the recipient's current balance is 0
     * 2. The recipient should have at least [minRentExemption] after the transaction
     * */
    fun isAmountValidForRecipient(
        inputAmount: BigInteger,
    ): Boolean {
        val isSourceTokenSol = tokenToSend.isSOL
        if (!isSourceTokenSol) return true

        val isRecipientEmpty = recipient is SearchResult.AddressFound && recipient.isEmptyBalance
        val isRecipientWillHaveMintExemption = inputAmount >= minRentExemption
        return isRecipientEmpty && isRecipientWillHaveMintExemption
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The sender is allowed to sent exactly the whole balance.
     * 2. It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    fun isAmountValidForSender(
        inputAmount: BigInteger,
    ): Boolean {
        if (!tokenToSend.isSOL) return true

        val balanceDiff = tokenToSend.totalInLamports - inputAmount
        return balanceDiff.isZero() || balanceDiff >= minRentExemption
    }

    /**
     * Validating only SOL -> SOL operations here
     * The empty recipient is required
     * Checking if the sender should leave at least [minRentExemption] or Zero SOL balance
     * */
    fun isMinRequiredBalanceLeft(
        inputAmountLamports: BigInteger,
    ): Boolean {
        if (!tokenToSend.isSOL) return true

        val isRecipientEmpty = recipient is SearchResult.AddressFound && recipient.isEmptyBalance

        val sourceTotalLamports = tokenToSend.totalInLamports
        val minRequiredBalance = minRentExemption

        val diff = sourceTotalLamports - inputAmountLamports

        val maxSolAmountAllowed = if (isRecipientEmpty) {
            // if recipient has no solana account (balance == 0) we can send at least minRentExemption amount
            sourceTotalLamports - minRequiredBalance
        } else {
            sourceTotalLamports
        }

        return diff.isZero() || maxSolAmountAllowed >= minRequiredBalance
    }
}
