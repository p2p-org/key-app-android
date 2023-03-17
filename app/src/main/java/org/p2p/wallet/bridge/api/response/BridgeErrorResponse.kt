package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName

data class BridgeErrorResponse(
    @SerializedName("error_code")
    val errorCode: Int? = null
)
