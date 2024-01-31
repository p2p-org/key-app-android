package org.p2p.wallet.send.model

import timber.log.Timber
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
class SendButtonStateMinSolValidator(
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
        val balanceRemaining = tokenToSend.totalInLamports - inputAmount
        val isSendingAllSol = balanceRemaining.isZero()
        val isRentExemptionRemaining = balanceRemaining >= minRentExemption
        val sendingMoreThanMinRent = inputAmount >= minRentExemption

        Timber.i(
            buildString {
                appendLine("$minRentExemption")
                appendLine("-----")
                appendLine("isRecipientEmpty = $isRecipientEmpty")
                appendLine("balanceRemaining = $balanceRemaining")
                appendLine("isSendingAllSol = $isSendingAllSol")
                appendLine("isRentExemptionRemaining = $isRentExemptionRemaining")
                appendLine("sendingMoreThanMinRent = $sendingMoreThanMinRent")
            }
        )

        val isSolAmountCanBeSent = isSendingAllSol || isRentExemptionRemaining

        if (isRecipientEmpty) {
            if (!sendingMoreThanMinRent) {
                return SendSolValidation.EmptyRecipientMinAmountInvalid
            }
            if (isSendingAllSol) {
                return SendSolValidation.AmountIsValid
            }

            if (!isRentExemptionRemaining && !isSendingAllSol) {
                return SendSolValidation.SenderNoRentExemptAmountLeft
            }
            if (!isSendingAllSol && !isRentExemptionRemaining) {
                return SendSolValidation.SenderNoRentExemptAmountLeft
            }
        }

        if (isSendingAllSol) {
            return SendSolValidation.AmountIsValid
        }
        // if we are not sending all SOL but leave some dust that less than rent
        if (!isRentExemptionRemaining) {
            return SendSolValidation.SenderNoRentExemptAmountLeft
        }
        return SendSolValidation.AmountIsValid
    }
}
