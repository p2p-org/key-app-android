package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

data class SendTransactionRequest(
    @SerializedName("instructions")
    val instructions: List<RequestInstruction>,
    @SerializedName("signatures")
    val signatures: Map<Int, String>,
    @SerializedName("pubkeys")
    val pubkeys: List<String>,
    @SerializedName("blockhash")
    val blockHash: String
)
