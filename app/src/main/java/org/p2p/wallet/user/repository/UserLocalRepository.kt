package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.receive.list.TokenListData

interface UserLocalRepository {
    /**
     * Check whether initial tokens from json file are loaded
     */
    fun areInitialTokensLoaded(): Boolean

    /**
     * Cache all available tokens (from json file currently)
     */
    fun setTokenData(data: List<TokenData>)
    fun getTokensData(): List<TokenData>

    fun fetchTokens(searchText: String, count: Int, refresh: Boolean)
    fun getTokenListFlow(): Flow<TokenListData>

    /**
     * Find [TokenData] by mint address
     */
    fun findTokenData(mintAddress: String): TokenData?

    /**
     * Find [TokenData] by its symbol
     */
    fun findTokenDataBySymbol(symbol: String): TokenData?

    /**
     * Find [Token] by its symbol
     */
    suspend fun findTokenByMint(mintAddress: String): Token?
}
