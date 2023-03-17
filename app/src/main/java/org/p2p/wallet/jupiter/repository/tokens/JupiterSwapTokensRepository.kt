package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
    suspend fun getTokenRate(token: JupiterSwapToken): TokenPrice?
}
