package org.p2p.wallet.infrastructure.network.feerelayer

import com.google.gson.annotations.SerializedName

data class FeeRelayerErrorDetails(
    val type: FeeRelayerErrorType,
    @SerializedName("ClientError")
    val clientError: List<String>?,
)
