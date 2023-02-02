package org.p2p.wallet.user.repository.prices

import org.p2p.wallet.home.model.TokenPrice

interface TokenPricesRemoteRepository {
    suspend fun getTokenPriceByIds(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice>

    suspend fun getTokenPriceById(
        tokenId: TokenId,
        targetCurrency: String
    ): TokenPrice
}
