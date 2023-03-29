package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

data class BridgeSendTransactionResponse(
    @SerializedName("transaction")
    val transaction: String,
    @SerializedName("message")
    val message: String?
)
