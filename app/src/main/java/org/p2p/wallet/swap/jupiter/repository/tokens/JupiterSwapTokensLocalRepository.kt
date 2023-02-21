package org.p2p.wallet.swap.jupiter.repository.tokens

import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

internal interface JupiterSwapTokensLocalRepository {
    fun setCachedTokens(tokens: List<JupiterSwapToken>)
    fun getCachedTokens(): List<JupiterSwapToken>?
}
