package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

class FreeTransactionFeeLimit(
    val maxUsage: Int,
    val currentUsage: Int,
    val maxAmount: BigInteger,
    val amountUsed: BigInteger
) {

    fun isFreeTransactionFeeAvailable(transactionFee: BigInteger, forNextTransaction: Boolean = false): Boolean {
        var currentUsage = currentUsage
        if (forNextTransaction) {
            currentUsage += 1
        }
        return currentUsage < maxUsage && (amountUsed + transactionFee) <= maxAmount
    }
}