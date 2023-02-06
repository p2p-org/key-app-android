package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.repository.model.JupiterToken

interface SwapTokensRepository {
    suspend fun getTokens(): List<JupiterToken>
}
