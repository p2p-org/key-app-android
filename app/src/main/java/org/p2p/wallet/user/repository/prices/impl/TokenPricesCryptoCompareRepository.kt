package org.p2p.wallet.user.repository.prices.impl

import com.google.gson.JsonObject
import kotlinx.coroutines.withContext
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.home.api.CryptoCompareApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.core.utils.Constants
import org.p2p.core.utils.scaleMedium

private const val COMPARE_API_CHUNK_SIZE = 30
private const val COMPARE_API_BODY_KEY = "Response"
private const val COMPARE_API_BODY_ERROR_VALUE = "Error"

class TokenPricesCryptoCompareRepository(
    private val cryptoCompareApi: CryptoCompareApi,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRemoteRepository {

    override suspend fun getTokenPriceById(
        tokenId: TokenId,
        targetCurrency: String
    ): TokenPrice = withContext(dispatchers.io) {
        val responseJson = cryptoCompareApi.getPrice(
            tokenFrom = tokenId.id,
            tokenTo = targetCurrency,
            apiKey = BuildConfig.comparePublicKey
        )

        val priceValue = responseJson.getAsJsonPrimitive(Constants.USD_READABLE_SYMBOL)

        TokenPrice(
            tokenId = tokenId.id,
            price = priceValue.asBigDecimal.scaleMedium()
        )
    }

    override suspend fun getTokenPriceByIds(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice> = withContext(dispatchers.io) {
        loadPrices(
            tokenIds = tokenIds,
            targetCurrencySymbol = targetCurrency
        )
    }

    private suspend fun loadPrices(tokenIds: List<TokenId>, targetCurrencySymbol: String): List<TokenPrice> {
        return tokenIds.map { it.id }
            .chunked(COMPARE_API_CHUNK_SIZE)
            .flatMap { chunkedTokenSymbols ->
                // CompareApi cannot resolve more than 30 token prices at once,
                // therefore we are splitting the token list
                val responseJson = cryptoCompareApi.getMultiPrice(
                    tokensFrom = chunkedTokenSymbols.joinToString(","),
                    tokenTo = targetCurrencySymbol,
                    apiKey = BuildConfig.comparePublicKey
                )
                parseResponseForChunk(responseJson, chunkedTokenSymbols)
            }
    }

    private fun parseResponseForChunk(response: JsonObject, chunkedTokenSymbols: List<String>): List<TokenPrice> {
        check(response[COMPARE_API_BODY_KEY]?.asString != COMPARE_API_BODY_ERROR_VALUE) {
            "Couldn't get rates for symbols: $chunkedTokenSymbols"
        }

        return chunkedTokenSymbols.mapNotNull { symbol ->
            response.getAsJsonObject(symbol.uppercase())?.let { priceJsonObject ->
                val priceValue = priceJsonObject.getAsJsonPrimitive(Constants.USD_READABLE_SYMBOL)
                TokenPrice(symbol, priceValue.asBigDecimal.scaleMedium())
            }
        }
    }
}
