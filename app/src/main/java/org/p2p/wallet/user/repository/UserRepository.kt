package org.p2p.wallet.user.repository

import org.p2p.core.token.TokenMetadata

interface UserRepository {
    suspend fun loadAllTokens(): List<TokenMetadata>
}
