package org.p2p.wallet.jupiter

import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.wallet.jupiter.model.SwapOpenedFrom

data class JupiterPresenterInitialData(
    val stateManagerHolderKey: String,
    val swapOpenedFrom: SwapOpenedFrom,
    val initialToken: Token.Active? = null,
    val initialAmountA: String? = null,
    val tokenAMint: Base58String? = null,
    val tokenBMint: Base58String? = null,
)
