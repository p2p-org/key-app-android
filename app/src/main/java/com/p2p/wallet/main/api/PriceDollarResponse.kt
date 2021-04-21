package com.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName

data class PriceDollarResponse(

    @SerializedName("USD")
    val value: Double
)