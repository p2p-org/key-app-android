package org.p2p.wallet.newsend.smartselection.validator

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.wallet.newsend.model.SearchResult

class SourceSolValidator(
    private val sourceToken: Token.Active,
    private val recipient: SearchResult,
    private val inputAmount: BigInteger,
    private val minRentExemption: BigInteger
) {

    /**
     * This case is only for sending SOL
     *
     * 1. The recipient should receive at least [minRentExemption] SOL balance if his current balance is 0
     * 2. The recipient should have at least [minRentExemption] after the transaction
     * */
    fun isAmountInvalidForRecipient(): Boolean {
        if (!sourceToken.isSOL) {
            return false
        }

        val isRecipientEmpty = recipient is SearchResult.AddressFound && recipient.isEmptyBalance

        if (!isRecipientEmpty) {
            return false
        }

        return inputAmount < minRentExemption
    }

    /**
     * This case is only for sending SOL
     *
     * 1. The sender is allowed to sent exactly the whole balance.
     * 2. It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    fun isAmountInvalidForSender(): Boolean {
        if (!sourceToken.isSOL) {
            return false
        }
        val balanceDiff = sourceToken.totalInLamports - inputAmount
        return balanceDiff.isNotZero() && balanceDiff < minRentExemption
    }

    /**
     * Validating only SOL -> SOL operations here
     * The empty recipient is required
     * Checking if the sender should leave at least [minRentExemption] or Zero SOL balance
     * */
    fun isLowMinBalanceIgnored(): Boolean {
        if (!sourceToken.isSOL) {
            return false
        }

        val isRecipientEmpty = recipient is SearchResult.AddressFound && recipient.isEmptyBalance

        val sourceTotalLamports = sourceToken.totalInLamports
        val minRequiredBalance = minRentExemption

        val diff = sourceTotalLamports - inputAmount

        val maxSolAmountAllowed = if (isRecipientEmpty) {
            // if recipient has no solana account (balance == 0) we can send at least minRentExemption amount
            sourceTotalLamports - minRequiredBalance
        } else {
            sourceTotalLamports
        }

        return diff.isNotZero() && maxSolAmountAllowed < minRequiredBalance
    }
}
