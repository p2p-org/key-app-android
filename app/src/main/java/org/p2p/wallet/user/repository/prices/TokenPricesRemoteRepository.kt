package org.p2p.wallet.user.repository.prices

import org.p2p.core.utils.Constants
import org.p2p.wallet.home.model.TokenPrice

interface TokenPricesRemoteRepository {
    suspend fun getTokenPriceByIds(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice>

    suspend fun getTokenPricesByIdsMap(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): Map<TokenId, TokenPrice>

    suspend fun getTokenPriceById(
        tokenId: TokenId,
        targetCurrency: String
    ): TokenPrice

    suspend fun getTokenPricesByAddressesMap(
        tokenAddresses: List<TokenAddress>,
        targetCurrency: String = Constants.USD_READABLE_SYMBOL
    ): Map<TokenAddress, TokenPrice>

    suspend fun getTokenPriceByAddress(
        tokenAddress: TokenAddress,
        targetCurrency: String = Constants.USD_READABLE_SYMBOL
    ): TokenPrice
}
