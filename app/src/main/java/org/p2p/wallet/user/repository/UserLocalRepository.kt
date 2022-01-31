package org.p2p.wallet.user.repository

import org.p2p.wallet.main.model.TokenPrice
import org.p2p.wallet.user.model.TokenData

interface UserLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun getPriceByToken(symbol: String): TokenPrice?

    fun setTokenData(data: List<TokenData>)
    fun getTokenData(): List<TokenData>
    fun findTokenData(mintAddress: String): TokenData?
    fun findTokenDataBySymbol(symbol: String): TokenData?
}