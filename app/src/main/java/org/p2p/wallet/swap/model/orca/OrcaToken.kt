package org.p2p.wallet.swap.model.orca

typealias Tokens = Map<String, OrcaToken>

data class OrcaToken(
    val mint: String,
    val name: String,
    val decimals: Int,
    val fetchPrice: Boolean?,
    val poolToken: Boolean?,
    val wrapper: String?
)