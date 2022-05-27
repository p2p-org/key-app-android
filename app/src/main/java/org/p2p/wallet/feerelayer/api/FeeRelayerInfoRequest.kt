package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class FeeRelayerInfoRequest(
    @SerializedName("operation_type")
    val operationType: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("build")
    val build: String
)
