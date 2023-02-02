package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryTokensResponse(
    @SerializedName("tokens_balance")
    val tokensBalanceResponse: RpcHistoryTokensBalanceResponse,
    @SerializedName("tokens_info")
    val tokensInfoResponse: RpcHistoryTokensInfoResponse
)
