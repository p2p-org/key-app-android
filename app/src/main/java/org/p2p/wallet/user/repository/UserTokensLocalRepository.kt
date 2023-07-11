package org.p2p.wallet.user.repository

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.token.service.model.TokenServicePrice

interface UserTokensLocalRepository {
    suspend fun updateTokens(tokens: List<Token.Active>)
    suspend fun getUserTokens(): List<Token.Active>
    fun observeUserTokens(): Flow<List<Token.Active>>

    suspend fun updateUserToken(
        newBalanceLamports: BigInteger,
        mintAddress: Base58String,
        publicKey: Base58String
    )
    fun observeUserToken(mintAddress: Base58String): Flow<Token.Active>

    fun observeUserBalance(): Flow<BigDecimal>
    suspend fun getUserBalance(): BigDecimal

    suspend fun removeIfExists(publicKey: String, symbol: String)
    suspend fun findTokenByMintAddress(mintAddress: Base58String): Token.Active?

    suspend fun saveRatesForTokens(prices: List<TokenServicePrice>)

    suspend fun clear()
}
