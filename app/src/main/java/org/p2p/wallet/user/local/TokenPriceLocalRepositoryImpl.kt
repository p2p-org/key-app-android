package org.p2p.wallet.user.local

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.home.model.TokenPrice

class TokenPriceLocalRepositoryImpl : TokenPriceLocalRepository {
    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun findTokenPriceByToken(symbol: String): TokenPrice? {
        return pricesFlow.value.firstOrNull { it.tokenSymbol == symbol }
    }
}
