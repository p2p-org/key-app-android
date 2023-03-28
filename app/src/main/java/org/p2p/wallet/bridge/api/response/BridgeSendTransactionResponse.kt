package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String

data class BridgeSendTransactionResponse(
    @SerializedName("transaction")
    val transaction: Base64String,
    @SerializedName("message")
    val message: String
)
