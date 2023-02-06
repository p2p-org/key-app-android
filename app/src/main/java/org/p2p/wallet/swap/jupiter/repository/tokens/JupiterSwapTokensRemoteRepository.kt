package org.p2p.wallet.swap.jupiter.repository.tokens

import org.p2p.core.utils.Constants
import org.p2p.core.utils.orZero
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.api.SwapJupiterApi
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext

class JupiterSwapTokensRemoteRepository(
    private val api: SwapJupiterApi,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val dispatchers: CoroutineDispatchers,
) : JupiterSwapTokensRepository {

    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.io) {
        val tokens = api.getSwapTokens()
        val prices = fetchPricesForTokens(tokens)
        tokens.toJupiterToken(prices)
    }

    private suspend fun fetchPricesForTokens(tokens: List<JupiterTokenResponse>): Map<TokenId, BigDecimal> {
        val tokensIds = tokens.mapNotNull { it.extensions.coingeckoId?.let(::TokenId) }
        return tokenPricesRepository.getTokenPricesByIdsMap(tokensIds, Constants.USD_SYMBOL)
            .mapValues { it.value.price }
    }

    private fun List<JupiterTokenResponse>.toJupiterToken(
        prices: Map<TokenId, BigDecimal>
    ): List<JupiterSwapToken> = map { response ->
        val tokenPrice = response.extensions.coingeckoId?.let { prices[TokenId(it)] }

        if (tokenPrice == null) {
            val errorMessage = buildString {
                append("Couldn't find any price for token with id: ${response.extensions.coingeckoId}; ")
                append("available prices: $prices")
            }
            Timber.i(errorMessage)
        }
        JupiterSwapToken(
            tokenMint = response.address.toBase58Instance(),
            chainId = response.chainId,
            decimals = response.decimals,
            coingeckoId = response.extensions.coingeckoId,
            logoUri = response.logoUri,
            tokenName = response.name,
            tokenSymbol = response.symbol,
            tags = response.tags,
            priceInUsd = tokenPrice.orZero()
        )
    }
}
