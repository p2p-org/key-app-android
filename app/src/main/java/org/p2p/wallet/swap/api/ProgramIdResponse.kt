package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

data class ProgramIdResponse(
    @SerializedName("serumTokenSwap")
    val serumTokenSwap: String,
    @SerializedName("tokenSwapV2")
    val tokenSwapV2: String,
    @SerializedName("tokenSwap")
    val tokenSwap: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("aquafarm")
    val aquafarm: String,
)
