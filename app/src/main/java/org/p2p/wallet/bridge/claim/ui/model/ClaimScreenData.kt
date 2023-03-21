package org.p2p.wallet.bridge.claim.ui.model

data class ClaimScreenData(
    val title: String,
    val tokenIconUrl: String?,
    val tokenFormattedAmount: String,
    val fiatFormattedAmount: String,
)
