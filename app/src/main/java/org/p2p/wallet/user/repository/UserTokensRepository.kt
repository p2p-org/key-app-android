package org.p2p.wallet.user.repository

import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey

interface UserTokensRepository {
    suspend fun loadUserTokens(publicKey: PublicKey): List<Token.Active>
}
