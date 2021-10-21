package com.p2p.wallet.main.repository

import com.p2p.wallet.main.model.Token
import kotlinx.coroutines.flow.Flow

interface MainLocalRepository {
    suspend fun setTokens(tokens: List<Token.Active>)
    suspend fun updateTokens(tokens: List<Token.Active>)
    fun getTokensFlow(): Flow<List<Token.Active>>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
}