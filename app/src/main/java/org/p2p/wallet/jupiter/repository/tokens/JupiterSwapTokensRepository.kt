package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

interface JupiterSwapTokensRepository {
    suspend fun findTokensExcludingMints(mintsToExclude: Set<Base58String>): List<JupiterSwapToken>
    suspend fun findTokensIncludingMints(mintsToInclude: Set<Base58String>): List<JupiterSwapToken>
    suspend fun findTokenByMint(mintAddress: Base58String): JupiterSwapToken?
    suspend fun requireTokenByMint(mintAddress: Base58String): JupiterSwapToken
    suspend fun findTokenBySymbol(symbol: String): JupiterSwapToken?

    /**
     * !! Avoid using this function, loading all tokens can take 5-10 seconds
     * it's better to use [findTokenByMint] or [requireTokenByMint] or whatever you need
     * @todo make this function available only with pagination
     */
    suspend fun getTokens(): List<JupiterSwapToken>

    suspend fun searchTokens(mintAddressOrSymbol: String): List<JupiterSwapToken>
    suspend fun searchTokensInSwappable(
        mintAddressOrSymbol: String,
        sourceTokenMint: Base58String
    ): List<JupiterSwapToken>

    suspend fun filterIntersectedTokens(userTokens: List<Token.Active>): List<Token.Active>
    suspend fun requireUsdc(): JupiterSwapToken
    suspend fun requireWrappedSol(): JupiterSwapToken
}
