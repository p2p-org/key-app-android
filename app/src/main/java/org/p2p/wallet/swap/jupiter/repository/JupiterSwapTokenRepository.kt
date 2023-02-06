package org.p2p.wallet.swap.jupiter.repository

import org.p2p.core.utils.Constants
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterTokensApi
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterToken
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext

class JupiterSwapTokenRepository(
    private val api: SwapJupiterTokensApi,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val dispatchers: CoroutineDispatchers,
) : SwapTokensRepository {

    override suspend fun getTokens(): List<JupiterToken> = withContext(dispatchers.io) {
        val tokens = api.getTokens()
        val prices = fetchPricesForTokens(tokens)
        tokens.toJupiterToken(prices)
    }

    private suspend fun fetchPricesForTokens(tokens: List<JupiterTokenResponse>): Map<TokenId, BigDecimal> {
        val tokensIds = tokens.map { it.extensions.coingeckoId.let(::TokenId) }
        return tokenPricesRepository.getTokenPricesByIdsMap(tokensIds, Constants.USD_SYMBOL)
            .mapValues { it.value.price }
    }

    private fun List<JupiterTokenResponse>.toJupiterToken(
        prices: Map<TokenId, BigDecimal>
    ): List<JupiterToken> = map { response ->
        val tokenId = TokenId(response.extensions.coingeckoId)
        val tokenPrice = prices[tokenId] ?: kotlin.run {
            Timber.i("Couldn't find any price for token with id ${tokenId.id}; available prices: $prices")
            BigDecimal.ZERO
        }
        JupiterToken(
            address = response.address,
            chainId = response.chainId,
            decimals = response.decimals,
            extensions = response.extensions,
            logoURI = response.logoUri,
            name = response.name,
            symbol = response.symbol,
            tags = response.tags,
            priceInUsd = tokenPrice
        )
    }
}
