package org.p2p.wallet.send.model

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero

sealed interface SendSolValidation {
    object AmountIsValid : SendSolValidation
    object EmptyRecipientMinAmountInvalid : SendSolValidation
    object SenderNotZeroed : SendSolValidation
    object SenderNoRentExemptAmountLeft : SendSolValidation
}

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
     *
     * 1. The sender is allowed to sent exactly the whole balance.
     * 2. It's allowed for the sender to have a SOL balance 0 or at least [minRentExemption]
     * */
    fun validateAmount(inputAmount: BigInteger): SendSolValidation {
        if (!tokenToSend.isSOL) return SendSolValidation.AmountIsValid

        val isRecipientEmpty = recipient is SearchResult.AddressFound && recipient.isEmptyBalance
        return if (isRecipientEmpty) {
            // rentExemption or more
            val sendingMoreThanMinRent = inputAmount >= minRentExemption
            if (sendingMoreThanMinRent) {
                SendSolValidation.AmountIsValid
            } else {
                SendSolValidation.EmptyRecipientMinAmountInvalid
            }
        } else {
            val balanceRemaining = tokenToSend.totalInLamports - inputAmount
            val isSendingFull = balanceRemaining.isZero()
            val isRentExemptionRemaining = balanceRemaining >= minRentExemption

            if (isRentExemptionRemaining || isSendingFull) {
                return SendSolValidation.AmountIsValid
            }
            if (!isRentExemptionRemaining) {
                return SendSolValidation.SenderNoRentExemptAmountLeft
            }
            // we are not emptying account to 0, we are left with some crumbs
            // that are less than minRentExemption
            if (!isSendingFull) {
                return SendSolValidation.SenderNotZeroed
            }
            return SendSolValidation.SenderNotZeroed
        }
    }
}
