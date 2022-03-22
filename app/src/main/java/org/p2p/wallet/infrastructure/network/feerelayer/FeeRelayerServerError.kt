package org.p2p.wallet.infrastructure.network.feerelayer

import com.google.gson.annotations.SerializedName

class FeeRelayerServerError(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FeeRelayerErrorDetails?
)
