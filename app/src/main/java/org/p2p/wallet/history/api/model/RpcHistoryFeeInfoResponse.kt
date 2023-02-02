package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryFeeInfoResponse(
    @SerializedName("fee_type")
    val feeType: String?,
    @SerializedName("fee_amount")
    val feeAmount: String?,
    @SerializedName("fee_payer")
    val feePayer: String?,
    @SerializedName("fee_token_price")
    val feeTokenPrice: String?
)
