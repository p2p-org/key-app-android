package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaWhitelistedAddressItemResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("network")
    val network: StrigaBlockchainNetworkResponse
)
