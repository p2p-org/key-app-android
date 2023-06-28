package org.p2p.wallet.striga.wallet.api.request

import com.google.gson.annotations.SerializedName

class StrigaGetWhitelistedAddressesRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("label")
    val label: String? = null,
)
