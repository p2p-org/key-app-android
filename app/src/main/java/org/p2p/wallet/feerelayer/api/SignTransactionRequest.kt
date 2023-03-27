package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

data class SignTransactionRequest(
    @SerializedName("instructions")
    val instructions: List<RequestInstruction>,
    @SerializedName("pubkeys")
    val pubkeys: List<String>,
    @SerializedName("blockhash")
    val blockHash: String,
    @SerializedName("info")
    val info: FeeRelayerInfoRequest
)
