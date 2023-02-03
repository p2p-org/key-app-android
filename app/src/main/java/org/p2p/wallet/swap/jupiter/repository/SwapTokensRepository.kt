package org.p2p.wallet.swap.jupiter.repository

import org.p2p.wallet.swap.jupiter.repository.model.SwapToken

interface SwapTokensRepository {
    suspend fun getTokens(): List<SwapToken>
}
