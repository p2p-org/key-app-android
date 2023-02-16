package org.p2p.wallet.auth.ui.verify

@Deprecated("Old onboarding flow, delete someday")
data class SecurityKeyTuple(
    var index: Int,
    var keys: List<Pair<String, Boolean>>
)
