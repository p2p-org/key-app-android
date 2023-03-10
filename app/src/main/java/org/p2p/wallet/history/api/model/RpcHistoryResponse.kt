package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryResponse(
    @SerializedName("items")
    val transactions: List<RpcHistoryTransactionResponse>,
)
