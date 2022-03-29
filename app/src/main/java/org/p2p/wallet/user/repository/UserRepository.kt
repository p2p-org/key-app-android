package org.p2p.wallet.user.repository

import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.user.model.TokenData

interface UserRepository {
    suspend fun loadAllTokens(): List<TokenData>
    suspend fun loadUserTokens(publicKey: String): List<Token.Active>
    suspend fun getRate(sourceSymbol: String, destinationSymbol: String): TokenPrice?
}
