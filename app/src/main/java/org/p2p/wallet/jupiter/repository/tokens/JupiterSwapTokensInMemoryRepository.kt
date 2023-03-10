package org.p2p.wallet.jupiter.repository.tokens

import timber.log.Timber
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

private const val TAG = "JupiterSwapTokensInMemoryRepository"

internal class JupiterSwapTokensInMemoryRepository : JupiterSwapTokensLocalRepository {
    private var cachedSwapTokens: List<JupiterSwapToken>? = null
        set(value) {
            Timber.tag(TAG).i("swap tokens updated: old:${field?.size}; new=${value?.size}")
            field = value
        }

    override fun getCachedTokens(): List<JupiterSwapToken>? = cachedSwapTokens

    override fun setCachedTokens(tokens: List<JupiterSwapToken>) {
        this.cachedSwapTokens = tokens
    }
}
