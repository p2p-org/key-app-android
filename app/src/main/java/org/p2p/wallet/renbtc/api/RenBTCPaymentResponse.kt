package org.p2p.wallet.renbtc.api

import com.google.gson.annotations.SerializedName

data class RenBTCPaymentResponse(
    @SerializedName("txid")
    val transactionHash: String,
    @SerializedName("vout")
    val txIndex: Int,
    @SerializedName("value")
    val amount: Long,
    @SerializedName("status")
    val status: RenBTCPaymentStatus,
)

data class RenBTCPaymentStatus(
    @SerializedName("confirmed")
    val confirmed: Boolean
)
