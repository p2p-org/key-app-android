package org.p2p.wallet.jupiter

import org.p2p.core.token.Token
import org.p2p.wallet.swap.ui.orca.SwapOpenedFrom

data class JupiterPresenterInitialData(
    val stateManagerHolderKey: String,
    val swapOpenedFrom: SwapOpenedFrom,
    val initialToken: Token.Active? = null,
    val initialAmountA: String? = null,
    val tokenASymbol: String? = null,
    val tokenBSymbol: String? = null,
)
