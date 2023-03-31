package org.p2p.wallet.feerelayer.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class FreeTransactionFeeLimit(
    val maxUsage: Int,
    val currentUsage: Int,

    val maxAmount: BigInteger,
    val amountUsed: BigInteger,

    val maxAccountCreationCount: Int,
    val accountCreationUsage: Int
) : Parcelable {

    @IgnoredOnParcel
    val remaining = maxUsage - currentUsage

    fun hasFreeTransactions(): Boolean = remaining != 0

    fun hasFreeAccountCreationUsages(): Boolean = maxAccountCreationCount - accountCreationUsage > 0

    fun isFreeTransactionFeeAvailable(transactionFee: BigInteger, forNextTransaction: Boolean = false): Boolean {
        var currentUsage = currentUsage
        if (forNextTransaction) {
            currentUsage += 1
        }
        return currentUsage < maxUsage && (amountUsed + transactionFee) <= maxAmount
    }
}
