package org.p2p.wallet.bridge.claim.model

import com.google.gson.annotations.SerializedName

enum class ClaimStatus() {
    @SerializedName("pending")
    PENDING(),

    @SerializedName("failed")
    FAILED(),

    @SerializedName("canceled")
    CANCELED(),

    @SerializedName("in_progress")
    IN_PROGRESS(),

    @SerializedName("completed")
    COMPLETED();

    fun canBeClaimed() = this != IN_PROGRESS && this != PENDING
}
