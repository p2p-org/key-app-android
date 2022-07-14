package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

class OrcaInfoResponse(
    @SerializedName("value")
    val config: OrcaConfigsResponse,
)
