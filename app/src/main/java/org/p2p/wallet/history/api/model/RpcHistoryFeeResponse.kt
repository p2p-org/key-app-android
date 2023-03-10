package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryFeeResponse(
    @SerializedName("amount")
    val amount: RpcHistoryAmountResponse? = null,
    @SerializedName("token")
    val token: RpcHistoryTokenResponse? = null,
    @SerializedName("payer")
    val payer: String,
    @SerializedName("type")
    val type: RpcHistoryFeeTypeResponse,
)
