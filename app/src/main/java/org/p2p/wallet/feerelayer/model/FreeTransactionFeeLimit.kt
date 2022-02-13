package org.p2p.wallet.feerelayer.model

class FreeTransactionFeeLimit(
    val maxUsage: Int,
    val currentUsage: Int
) {

    val canUseFeeRelayer: Boolean
        get() = currentUsage < maxUsage
}