package org.p2p.wallet.swap.jupiter.repository.tokens

import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
}
