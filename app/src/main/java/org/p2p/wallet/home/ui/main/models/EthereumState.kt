package org.p2p.wallet.home.ui.main.models

import org.p2p.core.token.Token
import org.p2p.wallet.bridge.claim.model.ClaimStatus

data class EthereumState(
    val ethereumTokens: List<Token.Eth> = emptyList(),
    val ethereumBundleStatuses: Map<String, ClaimStatus?> = emptyMap(),
)
