package org.p2p.wallet.user.local

import org.p2p.wallet.home.model.TokenPrice

interface TokenPriceLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun findTokenPriceByToken(symbol: String): TokenPrice?
}
