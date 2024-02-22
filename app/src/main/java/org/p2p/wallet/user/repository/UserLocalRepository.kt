package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadata
import org.p2p.wallet.receive.list.TokenListData

interface UserLocalRepository {
    /**
     * Check whether initial tokens from json file are loaded
     */
    fun areInitialTokensLoaded(): Boolean

    /**
     * Cache all available tokens (from json file currently)
     */
    fun setTokenData(data: List<TokenMetadata>)
    fun getTokensData(): List<TokenMetadata>

    fun fetchTokens(searchText: String, count: Int, refresh: Boolean)
    fun getTokenListFlow(): Flow<TokenListData>

    /**
     * Find [TokenMetadata] by mint address
     */
    fun findTokenData(mintAddress: String): TokenMetadata?

    /**
     * Find [TokenMetadata] by its symbol
     */
    @Deprecated("use findTokenData with mint address")
    fun findTokenDataBySymbol(symbol: String): TokenMetadata?

    /**
     * Find [Token] by its symbol
     */
    @Deprecated("This repository should not return [Token] objects but only [TokenMetadata]")
    suspend fun findTokenByMint(mintAddress: String): Token?
}
