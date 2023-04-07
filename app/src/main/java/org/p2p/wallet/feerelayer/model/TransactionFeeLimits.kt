package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class TransactionFeeLimits(
    val maxFeeCountAllowed: Int,
    val maxFeeAmountAllowed: BigInteger,
    val overallFeeCountUsed: Int,
    val overallFeeAmountUsed: BigInteger,

    val maxTransactionsAllowed: Int,
    val maxTransactionsAmountAllowed: BigInteger,
    val transactionsUsed: Int,
    val amountUsed: BigInteger,

    val maxAccountCreationCount: Int,
    val maxAccountCreationAmount: BigInteger,
    val accountCreationUsed: Int,
    val accountCreationAmountUsed: BigInteger
) : Parcelable {

    @IgnoredOnParcel
    val remaining = maxTransactionsAllowed - transactionsUsed

    fun isTransactionAllowed(): Boolean {
        val isTransactionCountEnough = maxTransactionsAllowed - transactionsUsed > 0
        val isTransactionAmountEnough = maxTransactionsAmountAllowed - amountUsed > BigInteger.ZERO
        return isTransactionCountEnough && isTransactionAmountEnough
    }

    fun isAccountCreationAllowed(): Boolean {
        val isAccountCreationCountEnough = maxAccountCreationCount - accountCreationUsed > 0

        val minRequiredAmount = accountCreationAmountUsed / accountCreationUsed.toBigInteger()
        // we need at least 2 039 280 lamports for next transaction
        val isAccountCreationAmountEnough = maxAccountCreationAmount - accountCreationAmountUsed > minRequiredAmount

        return isAccountCreationCountEnough && isAccountCreationAmountEnough
    }

    fun isFeeCoverAllowed(): Boolean {
        val isFeeCountEnough = maxFeeCountAllowed - overallFeeCountUsed > 0
        val isFeeAmountEnough = maxFeeAmountAllowed - overallFeeAmountUsed > BigInteger.ZERO
        return isFeeCountEnough && isFeeAmountEnough
    }

    fun isSendViaLinkAllowed(): Boolean = isAccountCreationAllowed() && isTransactionAllowed()

    fun isFreeTransactionFeeAvailable(transactionFee: BigInteger, forNextTransaction: Boolean = false): Boolean {
        var currentUsage = transactionsUsed
        if (forNextTransaction) {
            currentUsage += 1
        }
        return currentUsage < maxTransactionsAllowed && (amountUsed + transactionFee) <= maxTransactionsAmountAllowed
    }
}
