package org.p2p.wallet.home.ui.main.models

import org.p2p.core.token.Token

data class EthereumHomeState(
    val ethereumTokens: List<Token.Eth> = emptyList()
)
