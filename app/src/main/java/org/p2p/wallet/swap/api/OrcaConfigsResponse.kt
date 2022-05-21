package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

class OrcaConfigsResponse(
    @SerializedName("aquafarms")
    val aquafarms: Map<String, OrcaAquafarmResponse>,
    @SerializedName("pools")
    val pools: Map<String, OrcaPoolResponse>,
    @SerializedName("tokens")
    val tokens: Map<String, OrcaTokensResponse>,
    @SerializedName("programIds")
    val programIds: ProgramIdResponse,
    @SerializedName("collectibles")
    val collectibles: Map<String, OrcaCollectiblesResponse>,
)
