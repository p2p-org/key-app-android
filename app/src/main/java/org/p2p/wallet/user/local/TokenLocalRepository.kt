package org.p2p.wallet.user.local

import org.p2p.wallet.token.model.TokenData

interface TokenLocalRepository {
    fun setTokens(items: List<TokenData>)
    fun findTokenByMint(mint: String): TokenData?
    fun findTokenBySymbol(symbol: String): TokenData?
}
