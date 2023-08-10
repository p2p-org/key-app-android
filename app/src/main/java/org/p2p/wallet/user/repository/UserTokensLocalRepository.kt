package org.p2p.wallet.user.repository

import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.token.service.model.TokenServicePrice

interface UserTokensLocalRepository {
    fun observeUserTokens(): Flow<List<Token.Active>>
    fun observeUserToken(mintAddress: Base58String): Flow<Token.Active>
    suspend fun getUserTokens(): List<Token.Active>
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun updateOrCreateUserToken(tokenToUpdate: Token.Active)
    suspend fun updateTokenBalance(publicKey: Base58String, newTotal: BigDecimal, newTotalInUsd: BigDecimal?)
    suspend fun getUserBalance(): BigDecimal
    suspend fun removeIfExists(publicKey: String, symbol: String)
    suspend fun findTokenByMintAddress(mintAddress: Base58String): Token.Active?
    suspend fun saveRatesForTokens(prices: List<TokenServicePrice>)
    suspend fun setTokenHidden(mintAddress: String, visibility: String)
    suspend fun clear()
}
