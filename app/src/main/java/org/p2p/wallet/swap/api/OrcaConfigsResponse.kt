package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

class OrcaConfigsResponse(
    @SerializedName("pools")
    val pools: Map<String, OrcaPoolResponse>,
    @SerializedName("tokens")
    val tokens: Map<String, OrcaTokensResponse>,
    @SerializedName("programIds")
    val programIds: ProgramIdResponse
)
