package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryTokensInfoResponse(
    @SerializedName("swap_role")
    val swapRole: String?,
    @SerializedName("mint")
    val mint: String?,
    @SerializedName("symbol")
    val symbol: String?,
    @SerializedName("token_price")
    val tokenPrice: String?
)
