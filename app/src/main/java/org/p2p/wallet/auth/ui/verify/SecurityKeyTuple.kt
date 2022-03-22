package org.p2p.wallet.auth.ui.verify

data class SecurityKeyTuple(
    var index: Int,
    var keys: List<Pair<String, Boolean>>
)
