package org.p2p.wallet.user.local

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.token.model.TokenData

class TokenLocalRepositoryImpl: TokenLocalRepository {
    private val tokens = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokens(items: List<TokenData>) {
        tokens.value = items.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) {it.name})
    }

    override fun findTokenByMint(mint: String): TokenData? {
        return tokens.value.firstOrNull { it.mintAddress == mint }
    }

    override fun findTokenBySymbol(symbol: String): TokenData? {
        return tokens.value.firstOrNull { it.symbol == symbol }
    }
}
