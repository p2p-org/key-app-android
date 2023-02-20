package org.p2p.wallet.swap.jupiter.repository.tokens

import timber.log.Timber
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

private const val TAG = "JupiterSwapTokensInMemoryRepository"

internal class JupiterSwapTokensInMemoryRepository : JupiterSwapTokensLocalRepository {
    private var cachedTokens: MutableList<JupiterSwapToken> = mutableListOf()

    override fun getCachedTokens(): List<JupiterSwapToken> = cachedTokens

    override fun setCachedTokens(tokens: List<JupiterSwapToken>) {
        cachedTokens.clear()
        cachedTokens.addAll(tokens)
        Timber.tag(TAG).i("Jupiter cached swap tokens updated: size=${cachedTokens.size}")
    }
}
