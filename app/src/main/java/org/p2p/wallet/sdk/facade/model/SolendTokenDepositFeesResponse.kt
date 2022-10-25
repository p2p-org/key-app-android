package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName

class SolendTokenDepositFeesResponse(
    @SerializedName("fee")
    val accountCreationFee: Long,
    @SerializedName("rent")
    val rent: Long
)
