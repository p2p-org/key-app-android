package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

enum class BridgeSendStatusResponse {
    @SerializedName("pending")
    PENDING,

    @SerializedName("failed")
    FAILED,

    @SerializedName("in_progress")
    IN_PROGRESS,

    @SerializedName("completed")
    COMPLETED
}
