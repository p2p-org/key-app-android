package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.receive.list.TokenListData

interface UserLocalRepository {
    /**
     * Check whether initial tokens from json file are loaded
     */
    fun areInitialTokensLoaded(): Boolean

    /**
     * Check whether fiat prices are loaded
     */
    fun arePricesLoaded(): Boolean

    /**
     * Cache fiat prices
     */
    fun setTokenPrices(prices: List<TokenPrice>)

    /**
     * Get fiat prices flow
     */
    fun getTokenPrices(): Flow<List<TokenPrice>>

    /**
     * Get cached fiat prices
     */
    fun getCachedTokenPrices(): List<TokenPrice>

    /**
     * Find fiat price by token id (coingeckoId)
     */
    fun getPriceByTokenId(tokenId: String?): TokenPrice?

    /**
     * Cache all available tokens (from json file currently)
     */
    fun setTokenData(data: List<TokenData>)

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
    fun findTokenByMint(mintAddress: String): Token?
}
