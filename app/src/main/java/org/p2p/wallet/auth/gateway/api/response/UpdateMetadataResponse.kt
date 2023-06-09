package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName

data class UpdateMetadataResponse(
    @SerializedName("status")
    val status: Boolean,
)
