package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.core.crypto.Base58String
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

interface JupiterSwapTokensRepository {
    suspend fun getTokens(): List<JupiterSwapToken>
    suspend fun getTokenRate(token: JupiterSwapToken): TokenServicePrice?
    suspend fun getTokensRates(tokens: List<JupiterSwapToken>): Map<Base58String, TokenServicePrice>

    suspend fun findTokenBySymbol(symbol: String): JupiterSwapToken?
    suspend fun findTokenByMint(mintAddress: Base58String): JupiterSwapToken?
}
