package org.p2p.wallet.swap.model.orca

typealias OrcaRoute = List<String>
typealias OrcaRoutes = MutableMap<String, List<OrcaRoute>>

data class OrcaSwapInfo(
    val routes: OrcaRoutes,
    val tokens: OrcaTokens,
    val pools: OrcaPools,
    val programIds: OrcaProgramId,
    val tokenNames: Map<String, String>
)
