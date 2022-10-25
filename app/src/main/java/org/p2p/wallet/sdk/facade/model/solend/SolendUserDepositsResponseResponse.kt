package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

class SolendUserDepositsResponseResponse(
    @SerializedName("market_info")
    val deposits: List<SolendUserDepositResponse>
)
