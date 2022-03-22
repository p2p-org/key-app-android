package org.p2p.wallet.swap.model.orca

typealias OrcaAquafarms = MutableMap<String, OrcaAquafarm>

data class OrcaAquafarm(
    val account: String,
    val nonce: Int,
    val tokenProgramId: String,
    val emissionsAuthority: String,
    val removeRewardsAuthority: String,
    val baseTokenMint: String,
    val baseTokenVault: String,
    val rewardTokenMint: String,
    val rewardTokenVault: String,
    val farmTokenMint: String
)
