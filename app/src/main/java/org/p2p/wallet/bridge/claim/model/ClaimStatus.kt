package org.p2p.wallet.bridge.claim.model

import com.google.gson.annotations.SerializedName

enum class ClaimStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("failed")
    FAILED,

    @SerializedName("canceled")
    CANCELED,

    @SerializedName("in_progress")
    IN_PROGRESS,

    @SerializedName("completed")
    COMPLETED;
}

fun ClaimStatus?.canBeClaimed(): Boolean {
    if (this == null) {
        return true
    }
    return this != ClaimStatus.IN_PROGRESS && this != ClaimStatus.PENDING
}

fun ClaimStatus?.isProcessing(): Boolean {
    if (this == null) {
        return false
    }
    return this == ClaimStatus.IN_PROGRESS || this == ClaimStatus.PENDING
}
