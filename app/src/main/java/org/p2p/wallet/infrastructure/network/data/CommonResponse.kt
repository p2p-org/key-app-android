package org.p2p.wallet.infrastructure.network.data

import com.google.gson.annotations.SerializedName

data class CommonResponse<Result>(
    @SerializedName("result")
    val result: Result,

    @SerializedName("id")
    val id: String
)
