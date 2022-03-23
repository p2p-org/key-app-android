package org.p2p.wallet.swap.model.orca

typealias OrcaTokens = MutableMap<String, OrcaToken>

data class OrcaToken(
    val mint: String,
    val name: String,
    val decimals: Int,
    val fetchPrice: Boolean?,
    val poolToken: Boolean? = null,
    val wrapper: String? = null
)
