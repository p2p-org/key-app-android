package org.p2p.wallet.user.repository.prices

import org.p2p.wallet.home.model.TokenPrice

interface TokenPricesRepository {
    suspend fun getTokenPricesBySymbols(
        tokenSymbols: List<TokenSymbol>,
        targetCurrency: String
    ): List<TokenPrice>
}
