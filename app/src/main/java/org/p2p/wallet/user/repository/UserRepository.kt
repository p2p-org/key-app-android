package org.p2p.wallet.user.repository

import org.p2p.core.token.TokenData

interface UserRepository {
    suspend fun loadAllTokens(): List<TokenData>
}
