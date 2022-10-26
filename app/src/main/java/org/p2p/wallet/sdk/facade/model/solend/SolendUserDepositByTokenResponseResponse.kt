package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.annotations.SerializedName

class SolendUserDepositByTokenResponseResponse(
    @SerializedName("market_info")
    val userDepositBySymbol: SolendUserDepositResponse
)
