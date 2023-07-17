package org.p2p.wallet.striga.exchange.api.response

import com.google.gson.annotations.SerializedName

class StrigaExchangeRateItemResponse(
    @SerializedName("price")
    val price: String,
    @SerializedName("buy")
    val buy: String,
    @SerializedName("sell")
    val sell: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("currency")
    val currency: String,
)
