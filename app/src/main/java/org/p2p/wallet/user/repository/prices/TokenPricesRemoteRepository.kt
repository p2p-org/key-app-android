package org.p2p.wallet.user.repository.prices

import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.sources.CoinGeckoApiClient
import org.p2p.wallet.user.repository.prices.sources.CryptoCompareApiClient
import kotlinx.coroutines.withContext

class TokenPricesRemoteRepository(
    private val cryptoCompareApiClient: CryptoCompareApiClient,
    private val coinGeckoClient: CoinGeckoApiClient,
    private val appFeatureFlags: AppFeatureFlags,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRepository {

    override suspend fun getTokenPricesBySymbols(
        tokenSymbols: List<TokenSymbol>,
        targetCurrency: String
    ): List<TokenPrice> {
        return withContext(dispatchers.io) {
            if (appFeatureFlags.useCoinGeckoForPrices) {
                coinGeckoClient.loadPrices(
                    tokenSymbols = tokenSymbols,
                    targetCurrencySymbol = targetCurrency
                )
            } else {
                cryptoCompareApiClient.loadPrices(
                    tokenSymbols = tokenSymbols,
                    targetCurrencySymbol = targetCurrency
                )
            }
        }
    }
}
