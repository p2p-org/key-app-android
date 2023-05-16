package org.p2p.wallet.history.api.model

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class RpcHistoryTransactionResponse(
    @SerializedName("signature")
    val signature: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("status")
    val status: RpcHistoryStatusResponse,
    @SerializedName("type")
    val type: RpcHistoryTypeResponse?,
    @SerializedName("fees")
    val fees: List<RpcHistoryFeeResponse>,
    @SerializedName("block_number")
    val blockNumber: Long,
    @SerializedName("info")
    val info: JsonObject
)
