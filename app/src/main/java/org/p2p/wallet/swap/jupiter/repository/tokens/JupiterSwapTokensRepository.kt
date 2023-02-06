package org.p2p.wallet.swap.jupiter.repository.tokens

import org.p2p.wallet.swap.jupiter.repository.model.JupiterToken

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterToken>
}
