package com.p2p.wallet.main.repository

import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.main.model.TokenPrice
import kotlinx.coroutines.flow.Flow

interface MainLocalRepository {
    fun setTokens(tokens: List<Token>)
    fun getTokensFlow(): Flow<List<Token>>
    fun getTokens(): List<Token>

    fun setTokenPrices(tokens: List<TokenPrice>)
    fun getTokenPrices(): List<TokenPrice>
}