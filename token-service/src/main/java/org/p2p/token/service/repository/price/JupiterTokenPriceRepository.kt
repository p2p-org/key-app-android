package org.p2p.token.service.repository.price

import timber.log.Timber
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.utils.Constants
import org.p2p.token.service.api.JupiterPricesDataSource
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.model.TokenServiceQueryResult

internal class JupiterTokenPriceRepository(
    private val api: JupiterPricesDataSource,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun loadTokensPrice(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): TokenServiceQueryResult<TokenServicePrice> = withContext(dispatchers.io) {
        try {
            require(chain == TokenServiceNetwork.SOLANA) { "Only Solana tokens prices can be loaded" }

            val isNativeRequested = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN in addresses

            val mintsAsQuery = addresses.joinToString(separator = ",") {
                if (it == Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN) Constants.WRAPPED_SOL_MINT else it
            }
            val tokenMintsToRates = api.getPrices(mintsAsQuery).tokenMintsToPrices
                .mapValues { it.value.usdPrice }
                .run {
                    if (isNativeRequested) {
                        val nativeSolRate = Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN to this[Constants.WRAPPED_SOL_MINT]
                        this + nativeSolRate
                    } else {
                        this
                    }
                }

            val prices = addresses.map { mintAddress ->
                val rate = tokenMintsToRates[mintAddress]?.toBigDecimal()
                if (rate == null) {
                    Timber.e("USD rate for $mintAddress not found!")
                }

                TokenServicePrice(
                    tokenAddress = mintAddress,
                    rate = TokenRate(rate),
                    network = chain
                )
            }
            TokenServiceQueryResult(networkChain = chain, items = prices)
        } catch (error: Exception) {
            Timber.i(error, "Failed fetching prices")
            throw error
        }
    }
}
