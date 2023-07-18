package org.p2p.wallet.striga.exchange.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.MillisSinceEpoch

class StrigaExchangeRateItemResponse(
    @SerializedName("price")
    val price: String,
    @SerializedName("buy")
    val buyRate: String,
    @SerializedName("sell")
    val sellRate: String,
    @SerializedName("timestamp")
    val timestamp: MillisSinceEpoch,
    @SerializedName("currency")
    val currency: String,
)
