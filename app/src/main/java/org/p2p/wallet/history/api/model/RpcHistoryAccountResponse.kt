package org.p2p.wallet.history.api.model

import com.google.gson.annotations.SerializedName

data class RpcHistoryAccountResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("name")
    val username: String? = null
)
