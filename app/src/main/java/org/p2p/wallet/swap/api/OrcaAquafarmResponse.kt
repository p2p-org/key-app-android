package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

data class OrcaAquafarmResponse(
    @SerializedName("account")
    val account: String,
    @SerializedName("nonce")
    val nonce: Int,
    @SerializedName("tokenProgramId")
    val tokenProgramId: String,
    @SerializedName("emissionsAuthority")
    val emissionsAuthority: String,
    @SerializedName("removeRewardsAuthority")
    val removeRewardsAuthority: String,
    @SerializedName("baseTokenMint")
    val baseTokenMint: String,
    @SerializedName("baseTokenVault")
    val baseTokenVault: String,
    @SerializedName("rewardTokenMint")
    val rewardTokenMint: String,
    @SerializedName("rewardTokenVault")
    val rewardTokenVault: String,
    @SerializedName("farmTokenMint")
    val farmTokenMint: String,
)
