package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistorySwapTokenResponse(
    @SerializedName("token")
    val token: RpcHistoryTokenResponse,
    @SerializedName("amount")
    val amounts: RpcHistoryAmountResponse
)
