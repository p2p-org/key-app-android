package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaWhitelistedAddressesResponse(
    @SerializedName("addresses")
    val addresses: List<StrigaWhitelistedAddressItemResponse>,
    @SerializedName("count")
    val count: Int,
    @SerializedName("total")
    val total: Int
)
