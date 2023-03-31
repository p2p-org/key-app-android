package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.utils.Base58String

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
    suspend fun getTokenRate(token: JupiterSwapToken): TokenPrice?
    suspend fun getTokensRates(tokens: List<JupiterSwapToken>): Map<Base58String, TokenPrice>
}
