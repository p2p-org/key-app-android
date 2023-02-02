package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryCounterPartyResponse(
    @SerializedName("address")
    val address: String?,
    @SerializedName("username")
    val username: String?
)
