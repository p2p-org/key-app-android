package org.p2p.wallet.token.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.token.model.TokenData

interface TokenRepository {
    suspend fun loadAllTokens(): List<TokenData>
    suspend fun loadUserTokens(publicKey: String, fetchPrices: Boolean): List<Token.Active>
}
