package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

interface JupiterTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
}
