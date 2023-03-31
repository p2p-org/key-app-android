package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName
import org.p2p.solanaj.utils.crypto.Base64String

data class SignTransactionRequest(
    @SerializedName("transaction")
    val transaction: Base64String,
    @SerializedName("info")
    val info: FeeRelayerInfoRequest
)
