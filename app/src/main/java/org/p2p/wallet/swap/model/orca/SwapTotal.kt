package org.p2p.wallet.swap.model.orca

data class SwapTotal(
    val destinationAmount: String,
    val total: String,
    val totalUsd: String?,
    val fee: String?,
    val approxFeeUsd: String,
    val receiveAtLeast: String,
    val receiveAtLeastUsd: String?
) {

    val fullTotal: String
        get() = if (approxTotalUsd != null) "$total $approxTotalUsd" else total

    val approxTotalUsd: String? get() = totalUsd?.let { "(~$it)" }

    val fullFee: String?
        get() = fee?.let { "$it $approxFeeUsd" }

    val fullReceiveAtLeast: String
        get() = if (approxReceiveAtLeast != null) "$receiveAtLeast $approxReceiveAtLeast" else receiveAtLeast

    val approxReceiveAtLeast: String?
        get() = receiveAtLeastUsd?.let { "(~$it)" }
}
