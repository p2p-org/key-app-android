package org.p2p.wallet.main.model

class SendTotal(
    val total: String,
    val totalUsd: String?,
    val receive: String,
    val receiveUsd: String?,
    val fee: String?,
    val feeUsd: String?,
    val accountCreationFee: String?,
    val accountCreationFeeUsd: String?
) {

    val fullTotal: String
        get() = if (approxTotalUsd != null) "$total $approxTotalUsd" else total

    val approxTotalUsd: String? get() = totalUsd?.let { "(~$$it)" }

    val fullReceive: String
        get() = if (approxReceive != null) "$receive $approxReceive" else receive

    val approxReceive: String?
        get() = receiveUsd?.let { "(~$$it)" }

    val fullFee: String?
        get() = fee?.let { "$it $approxFeeUsd" }

    val approxFeeUsd: String? get() = feeUsd?.let { "(~$$it)" }
}