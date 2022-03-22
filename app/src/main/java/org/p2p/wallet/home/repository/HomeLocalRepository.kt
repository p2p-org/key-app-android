package org.p2p.wallet.home.repository

import org.p2p.wallet.home.model.Token
import kotlinx.coroutines.flow.Flow

interface HomeLocalRepository {
    suspend fun setTokens(tokens: List<Token.Active>)
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun removeIfExists(publicKey: String, symbol: String)
    fun getTokensFlow(): Flow<List<Token.Active>>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
}
