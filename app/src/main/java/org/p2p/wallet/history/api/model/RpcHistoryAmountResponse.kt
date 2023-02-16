package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryAmountResponse(
    @SerializedName("amount")
    val amount: String? = null,
    @SerializedName("usd_amount")
    val usdAmount: String? = null
)
