package org.p2p.wallet.jupiter.repository.tokens

import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.common.date.toDateTimeString
import org.p2p.wallet.common.date.toZonedDateTime
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.api.SwapJupiterApi
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.prices.TokenCoinGeckoId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.toBase58Instance

internal class JupiterSwapTokensRemoteRepository(
    private val api: SwapJupiterApi,
    private val localRepository: JupiterSwapTokensLocalRepository,
    private val dispatchers: CoroutineDispatchers,
    private val userRepository: UserLocalRepository,
    private val pricesRepository: TokenPricesRemoteRepository,
    private val swapStorage: JupiterSwapStorageContract,
    private val pricesCache: JupiterSwapTokensPricesLocalRepository
) : JupiterSwapTokensRepository {

    override suspend fun getTokens(): List<JupiterSwapToken> = withContext(dispatchers.io) {
        getSwapTokensFromCache() ?: fetchTokensFromRemote().also(::saveToStorage)
    }

    private fun getSwapTokensFromCache(): List<JupiterSwapToken>? {
        if (!isCacheCanBeUsed()) {
            Timber.i("Cannot use the cache for swap tokens")
            return null
        }
        Timber.i("Cache is valid, using cache")
        return localRepository.getCachedTokens()
            ?: swapStorage.swapTokens?.also(localRepository::setCachedTokens)
    }

    private suspend fun fetchTokensFromRemote(): List<JupiterSwapToken> {
        Timber.i("Fetching new routes, cache is empty")
        return api.getSwapTokens().toJupiterToken()
    }

    private fun saveToStorage(tokens: List<JupiterSwapToken>) {
        swapStorage.swapTokensFetchDateMillis = System.currentTimeMillis()
        swapStorage.swapTokens = tokens

        val updateDate = swapStorage.swapTokensFetchDateMillis?.toZonedDateTime()?.toDateTimeString()
        Timber.i("Updated tokens cache: date=$updateDate; routes=${tokens.size} ")
    }

    private fun List<JupiterTokenResponse>.toJupiterToken(): List<JupiterSwapToken> = map { response ->
        val tokenLogoUri = userRepository.findTokenData(response.address)?.iconUrl ?: response.logoUri.orEmpty()
        JupiterSwapToken(
            tokenMint = response.address.toBase58Instance(),
            chainId = response.chainId,
            decimals = response.decimals,
            coingeckoId = response.extensions?.coingeckoId,
            logoUri = tokenLogoUri,
            tokenName = response.name,
            tokenSymbol = response.symbol,
            tags = response.tags,
        )
    }

    private fun isCacheCanBeUsed(): Boolean {
        val fetchTokensDate = swapStorage.swapTokensFetchDateMillis ?: return false
        val now = System.currentTimeMillis()
        return (now - fetchTokensDate) <= TimeUnit.DAYS.toMillis(1) // check day has passed
    }

    override suspend fun getTokensRates(tokens: List<JupiterSwapToken>): Map<Base58String, TokenPrice> {
        val tokenMints = tokens.map(JupiterSwapToken::tokenMint)
        val tokensCoingeckoIds = tokens.mapNotNull { it.coingeckoId?.let(::TokenCoinGeckoId) }

        if (tokensCoingeckoIds.isEmpty()) {
            return emptyMap()
        }
        val isTokenPricesCachedForMints = tokenMints.all { it in pricesCache }
        if (isTokenPricesCachedForMints) {
            return tokenMints.associateWith(pricesCache::requirePriceByMint)
        }
        return try {
            pricesRepository.getTokenPricesByIdsMap(
                tokenIds = tokensCoingeckoIds,
                targetCurrency = USD_READABLE_SYMBOL
            )
                .mapKeys { (tokenId) -> tokens.first { it.coingeckoId == tokenId.id }.tokenMint }
                .also(pricesCache::update)
        } catch (error: Throwable) {
            // coingecko can return empty price: []
            Timber.i(error)
            emptyMap()
        }
    }

    override suspend fun getTokenRate(token: JupiterSwapToken): TokenPrice? {
        return getTokensRates(listOf(token))[token.tokenMint]
    }
}
