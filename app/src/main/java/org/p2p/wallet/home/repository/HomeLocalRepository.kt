package org.p2p.wallet.home.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token

interface HomeLocalRepository {
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun removeIfExists(publicKey: String, symbol: String)
    fun getTokensFlow(): Flow<List<Token.Active>>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
}
