package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

class FreeTransactionFeeLimit(
    val maxUsage: Int,
    val currentUsage: Int,
    val maxAmount: BigInteger,
    val amountUsed: BigInteger
) {

    val canUseFeeRelayer: Boolean
        get() = currentUsage < maxUsage

    fun isFreeTransactionFeeAvailable(transactionFee: BigInteger): Boolean =
        currentUsage < maxUsage && (amountUsed + transactionFee) <= maxAmount
}