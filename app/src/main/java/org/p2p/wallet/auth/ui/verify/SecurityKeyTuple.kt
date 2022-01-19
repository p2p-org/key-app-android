package org.p2p.wallet.auth.ui.verify

data class SecurityKeyTuple(
    val index: Int,
    val keys: List<Pair<String, Boolean>>
)