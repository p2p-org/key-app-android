package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

class SolendTokenDepositFeesResponse(
    @SerializedName("fee")
    val fee: Long,
    @SerializedName("rent")
    val rent: Long
)
