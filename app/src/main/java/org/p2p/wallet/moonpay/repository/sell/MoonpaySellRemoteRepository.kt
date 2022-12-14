package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayIpAddressResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.serversideapi.MoonpayServerSideApi
import org.p2p.wallet.utils.Base58String
import timber.log.Timber
import kotlinx.coroutines.withContext

private const val TAG = "MoonpaySellRemoteRepository"

class MoonpaySellRemoteRepository(
    private val moonpayClientSideApi: MoonpayClientSideApi,
    private val moonpayServerSideApi: MoonpayServerSideApi,
    private val crashLogger: CrashLogger,
    private val mapper: MoonpaySellRepositoryMapper,
    private val dispatchers: CoroutineDispatchers,
) : MoonpaySellRepository {

    private class MoonpayRepositoryInternalError(override val cause: Throwable) : Throwable(cause.message)

    // todo: maybe extract caching flags to a separate repository to reuse
    private var cachedMoonpayIpFlags: MoonpayIpAddressResponse? = null

    override suspend fun loadMoonpayFlags() {
        withContext(dispatchers.io) {
            try {
                cachedMoonpayIpFlags = moonpayClientSideApi.getIpAddress(BuildConfig.moonpayKey)
                Timber.i("Moonpay IP flags were fetched successfully")
            } catch (e: Throwable) {
                Timber.e(MoonpayRepositoryInternalError(e))
            }
        }
    }

    override fun isSellAllowedForUser(): Boolean {
        val ipFlags = cachedMoonpayIpFlags
        if (ipFlags == null) {
            Timber.e(MoonpayRepositoryInternalError(IllegalStateException("Moonpay IP flags were not fetched")))
            crashLogger.setCustomKey("is_moonpay_sell_enabled", false)
            crashLogger.setCustomKey("country_from_moonpay", false)
            return false
        }

        crashLogger.setCustomKey("is_moonpay_sell_enabled", ipFlags.isSellAllowed)
        crashLogger.setCustomKey("country_from_moonpay", ipFlags.currentCountryAbbreviation)

        return ipFlags.isSellAllowed
    }

    @Throws(MoonpaySellError::class)
    override suspend fun getUserSellTransactions(
        userAddress: Base58String
    ): List<MoonpaySellTransaction> = doMoonpayRequest {
        mapper.fromNetwork(
            response = moonpayServerSideApi.getUserSellTransactions(userAddress.base58Value),
            transactionOwnerAddress = userAddress
        )
    }

    @Throws(MoonpaySellError::class)
    override suspend fun getSellQuoteForToken(
        tokenToSell: Token.Active,
        tokenAmount: Double,
        fiat: MoonpaySellFiatCurrency
    ): MoonpaySellTokenQuote = doMoonpayRequest {
        mapper.fromNetwork(
            moonpayClientSideApi.getSellQuoteForToken(
                tokenSymbol = tokenToSell.tokenSymbol.lowercase(),
                apiKey = BuildConfig.moonpayKey,
                fiatName = fiat.symbol,
                tokenAmount = tokenAmount
            )
        )
    }

    override suspend fun getCurrentCountryAbbreviation(): String {
        return cachedMoonpayIpFlags?.currentCountryAbbreviation.orEmpty()
    }

    @Throws(MoonpaySellError::class)
    private suspend inline fun <R> doMoonpayRequest(
        crossinline request: suspend () -> R
    ): R = withContext(dispatchers.io) {
        try {
            request.invoke()
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Moonpay request failed")
            throw mapper.fromNetworkError(error)
        }
    }
}
