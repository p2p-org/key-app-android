package org.p2p.wallet.main.repository

import org.p2p.wallet.main.model.Token
import kotlinx.coroutines.flow.Flow

interface MainLocalRepository {
    suspend fun setTokens(tokens: List<Token.Active>)
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun removeTemporarySol(publicKey: String)
    fun getTokensFlow(): Flow<List<Token.Active>>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
}