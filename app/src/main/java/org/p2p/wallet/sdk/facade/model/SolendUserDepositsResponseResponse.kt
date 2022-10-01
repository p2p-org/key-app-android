package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName

class SolendUserDepositsResponseResponse(
    @SerializedName("market_info")
    val deposits: List<SolendUserDepositResponse>
)
