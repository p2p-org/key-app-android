package org.p2p.wallet.home.repository

import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import org.p2p.core.token.Token
import org.p2p.core.crypto.Base58String

interface UserTokensRepository {
    fun observeUserTokens(): Flow<List<Token.Active>>
    fun observeUserToken(tokenMint: Base58String): Flow<Token.Active>
    suspend fun updateUserToken(newBalanceLamports: BigInteger, tokenMint: Base58String, accountPublicKey: Base58String)
    suspend fun findUserTokenByMint(mintAddress: Base58String): Token.Active?
}