package org.p2p.wallet.user.repository.prices

import org.p2p.wallet.home.model.TokenPrice

interface TokenPricesRemoteRepository {
    suspend fun getTokenPriceByIds(
        tokenIds: List<TokenCoinGeckoId>,
        targetCurrency: String
    ): List<TokenPrice>

    suspend fun getTokenPricesByIdsMap(
        tokenIds: List<TokenCoinGeckoId>,
        targetCurrency: String
    ): Map<TokenCoinGeckoId, TokenPrice>

    suspend fun getTokenPriceById(
        tokenId: TokenCoinGeckoId,
        targetCurrency: String
    ): TokenPrice
}
