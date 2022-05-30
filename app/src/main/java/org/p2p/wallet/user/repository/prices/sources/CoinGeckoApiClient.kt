package org.p2p.wallet.user.repository.prices.sources

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.user.repository.prices.TokenSymbol
import org.p2p.wallet.utils.scaleMedium
import timber.log.Timber

private const val COIN_GECKO_TOKENS_FILE_NAME = "coin_gecko_tokens.json"

class CoinGeckoApiClient(
    private val coinGeckoApi: CoinGeckoApi,
    private val fileRepository: FileRepository,
    private val gson: Gson
) {

    private class CoinGeckoTokenInformation(
        @SerializedName("id")
        val tokenId: String,
        @SerializedName("symbol")
        val tokenSymbol: String
    )

    /**
     * CoinGecko has rate limit of 50 calls/minute FYI
     */
    suspend fun loadPrices(tokenSymbols: List<TokenSymbol>, targetCurrencySymbol: String): List<TokenPrice> {
        if (!fileRepository.isFileExists(COIN_GECKO_TOKENS_FILE_NAME)) {
            cacheCoinGeckoTokensSymbols()
        }

        val validatedTargetCurrency = targetCurrencySymbol.lowercase()

        val coinGeckoTokens = getCoinGeckoTokenSymbols()

        val coinGeckoTokenIdToStandardSymbol = mapSymbolToCoinGeckoTokenIds(tokenSymbols, coinGeckoTokens)

        val tokenIdsForRequest = coinGeckoTokenIdToStandardSymbol.keys.joinToString(separator = ",")

        return coinGeckoApi.getTokenPrices(
            tokenIds = tokenIdsForRequest,
            targetCurrency = validatedTargetCurrency
        )
            .mapNotNull { (token, currencies) ->
                if (token in coinGeckoTokenIdToStandardSymbol && validatedTargetCurrency in currencies) {
                    TokenPrice(
                        tokenSymbol = coinGeckoTokenIdToStandardSymbol.getValue(token).symbol,
                        price = currencies.getValue(validatedTargetCurrency).toBigDecimal().scaleMedium()
                    )
                } else {
                    null
                }
            }
    }

    private suspend fun cacheCoinGeckoTokensSymbols() {
        kotlin.runCatching {
            val allTokensJson = coinGeckoApi.getAllTokens()
            fileRepository.saveFileToMisc(COIN_GECKO_TOKENS_FILE_NAME, allTokensJson.byteStream())
        }
            .onFailure { Timber.e(it) }
    }

    private fun getCoinGeckoTokenSymbols(): List<CoinGeckoTokenInformation> {
        return fileRepository.getFileFromMisc(COIN_GECKO_TOKENS_FILE_NAME)
            ?.inputStream()
            ?.readBytes()
            ?.let { jsonBytes ->
                val listType = object : TypeToken<List<CoinGeckoTokenInformation>>() {}.type
                gson.fromJson<List<CoinGeckoTokenInformation>>(String(jsonBytes), listType)
            }
            .orEmpty()
    }

    private fun mapSymbolToCoinGeckoTokenIds(
        symbols: List<TokenSymbol>,
        coinGeckoTokens: List<CoinGeckoTokenInformation>
    ): Map<String, TokenSymbol> {
        return symbols.associateBy(
            keySelector = { standardTokenSymbol ->
                coinGeckoTokens.firstOrNull { coinGeckoToken ->
                    coinGeckoToken.tokenSymbol.contentEquals(standardTokenSymbol.symbol, ignoreCase = true)
                }?.tokenId.orEmpty()
            },
            valueTransform = { it }
        )
    }
}
