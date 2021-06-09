package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.TokenBid
import com.p2p.wallet.user.model.TokenData

interface UserLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun getPriceByToken(token: String): TokenPrice

    fun setTokenBids(bids: List<TokenBid>)
    fun getBidByToken(token: String): TokenBid

    fun setTokenDecimals(decimals: List<TokenData>)
    fun getDecimalsByToken(mint: String): TokenData?
}