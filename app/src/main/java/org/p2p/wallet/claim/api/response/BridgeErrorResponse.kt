package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName

data class BridgeErrorResponse(
    @SerializedName("errorCode")
    val errorCode: Int? = null
)
