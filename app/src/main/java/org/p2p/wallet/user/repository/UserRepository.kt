package org.p2p.wallet.user.repository

import org.p2p.core.token.Token
import org.p2p.core.token.TokenData

interface UserRepository {
    suspend fun loadAllTokens(): List<TokenData>
    suspend fun loadUserTokens(publicKey: String): List<Token.Active>
}
