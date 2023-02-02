package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryTokensBalanceResponse(
    @SerializedName("balance_before")
    val balanceBefore: String?,
    @SerializedName("balance_after")
    val balanceAfter: String?
)
