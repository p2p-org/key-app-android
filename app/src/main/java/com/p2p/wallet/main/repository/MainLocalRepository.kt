package com.p2p.wallet.main.repository

import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.main.model.TokenPrice
import kotlinx.coroutines.flow.Flow

interface MainLocalRepository {
    suspend fun setTokens(tokens: List<Token>)
    suspend fun getTokensFlow(): Flow<List<Token>>
    suspend fun getTokens(): List<Token>

    suspend fun setTokenPrices(tokens: List<TokenPrice>)
    suspend fun getTokenPrices(): List<TokenPrice>
}