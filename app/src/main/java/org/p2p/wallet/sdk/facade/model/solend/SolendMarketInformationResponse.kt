package org.p2p.wallet.sdk.facade.model.solend

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

class SolendMarketInformationResponse(
    @SerializedName("market_info")
    val marketInfo: JsonArray
)
