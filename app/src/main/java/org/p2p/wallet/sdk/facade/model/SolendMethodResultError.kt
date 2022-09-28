package org.p2p.wallet.sdk.facade.model

import com.google.gson.annotations.SerializedName

data class SolendMethodResultError(
    @SerializedName("error")
    val error: String,
) : Throwable(message = error)
