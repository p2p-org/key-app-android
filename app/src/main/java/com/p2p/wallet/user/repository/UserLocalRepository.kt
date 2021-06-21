package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.TokenData
import kotlinx.coroutines.flow.Flow

interface UserLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun getPriceByToken(token: String): TokenPrice

    fun setTokenData(decimals: List<TokenData>)
    fun getTokenData(mintAddress: String): TokenData?
    fun getTokenDataFlow(): Flow<List<TokenData>>
}