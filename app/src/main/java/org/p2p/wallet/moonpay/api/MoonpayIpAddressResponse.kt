package org.p2p.wallet.moonpay.api

import com.google.gson.annotations.SerializedName

class MoonpayIpAddressResponse(
    @SerializedName("alpha3")
    val currentCountryAbbreviation: String,
    @SerializedName("isAllowed")
    val isAllowed: Boolean,
    @SerializedName("isBuyAllowed")
    val isBuyAllowed: Boolean,
    @SerializedName("isSellAllowed")
    val isSellAllowed: Boolean
)
