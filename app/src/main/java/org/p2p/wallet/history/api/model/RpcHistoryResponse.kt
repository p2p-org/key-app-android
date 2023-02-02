package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("status")
    val stauts: RpcHistoryStatusResponse,
    @SerializedName("tx_transaction")
    val txTransaction: String,
    @SerializedName("transaction_type")
    val transactionType: RpcHistoryTypeResponse,
    @SerializedName("transaction_info")
    val transactionInfo: RpcHistoryInfoResponse
)
