package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaAddWhitelistedAddressRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("address")
    val addressToWhitelist: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("network")
    val network: String,
    @SerializedName("label")
    val label: String? = null
)
