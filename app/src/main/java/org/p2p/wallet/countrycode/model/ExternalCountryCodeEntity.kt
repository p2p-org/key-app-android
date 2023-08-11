package org.p2p.wallet.countrycode.model

import com.google.gson.annotations.SerializedName

class ExternalCountryCodeEntity(
    @SerializedName("name")
    val countryName: String,
    @SerializedName("alpha2")
    val nameCodeAlpha2: String,
    @SerializedName("alpha3")
    val nameCodeAlpha3: String,
    @SerializedName("flag_emoji")
    val flagEmoji: String?,
    @SerializedName("is_striga_allowed")
    val isStrigaAllowed: Boolean,
    @SerializedName("is_moonpay_allowed")
    val isMoonpayAllowed: Boolean
)
