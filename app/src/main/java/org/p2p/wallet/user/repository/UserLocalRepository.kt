package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.receive.list.TokenListData

interface UserLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun getTokenPrices(): Flow<List<TokenPrice>>
    fun getPriceByTokenId(tokenId: String?): TokenPrice?

    fun setTokenData(data: List<TokenData>)
    fun fetchTokens(searchText: String, count: Int, refresh: Boolean)
    fun getTokenListFlow(): Flow<TokenListData>
    fun findTokenData(mintAddress: String): TokenData?
    fun findTokenDataBySymbol(symbol: String): TokenData?
    fun findTokenByMint(mintAddress: String): Token?
}
