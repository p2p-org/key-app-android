package org.p2p.wallet.striga.wallet.api.response

import com.google.gson.annotations.SerializedName

class StrigaBlockchainNetworkResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("contractAddress")
    val contractAddress: String?,
    @SerializedName("type")
    val type: String?
)
