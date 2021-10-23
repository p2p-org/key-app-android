package org.p2p.wallet.swap.model.orca

typealias Route = List<String>
typealias Routes = Map<String, List<Route>>

data class OrcaSwapInfo(
    val routes: Routes,
    val tokens: Tokens
)