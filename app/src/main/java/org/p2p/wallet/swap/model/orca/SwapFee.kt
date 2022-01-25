package org.p2p.wallet.swap.model.orca

data class SwapFee(
    val currentFeePayToken: String,
    val accountCreationToken: String?,
    val accountCreationFee: String?,
    private val accountCreationFeeUsd: String?
) {

    val commonFee: String?
        get() = accountCreationFee?.let { "$it $approxFeeUsd" }

    val approxFeeUsd: String?
        get() = accountCreationFeeUsd?.let { "~($it)" }
}