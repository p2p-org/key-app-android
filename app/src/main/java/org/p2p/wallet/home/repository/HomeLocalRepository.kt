package org.p2p.wallet.home.repository

import org.p2p.core.token.Token
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

interface HomeLocalRepository {
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun removeIfExists(publicKey: String, symbol: String)
    fun getTokensFlow(): Flow<List<Token.Active>>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
    fun observeUserBalance(): Flow<BigDecimal>
    suspend fun getUserBalance(): BigDecimal
}
