package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.clientsideapi.MoonpayClientSideApi
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayIpAddressResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTokenQuote
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpaySellTransactionDepositWalletResponse
import org.p2p.wallet.moonpay.model.MoonpaySellError
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.serversideapi.MoonpayServerSideApi
import org.p2p.wallet.moonpay.serversideapi.response.MoonpaySellTransactionShortResponse
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.utils.Base58String
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

private const val TAG = "MoonpaySellRemoteRepository"

class MoonpaySellRemoteRepository(
    private val moonpayClientSideApi: MoonpayClientSideApi,
    private val moonpayServerSideApi: MoonpayServerSideApi,
    private val crashLogger: CrashLogger,
    private val mapper: SellRepositoryMapper,
    private val externalCustomerIdProvider: MoonpayExternalCustomerIdProvider,
    private val errorMapper: SellRepositoryErrorMapper,
    private val dispatchers: CoroutineDispatchers,
) : SellRepository {

    private class MoonpayRepositoryInternalError(override val cause: Throwable) : Throwable(cause.message)

    // todo: maybe extract caching flags to a separate repository to reuse
    private var cachedMoonpayIpFlags: MoonpayIpAddressResponse? = null

    private val externalCustomerId: String
        get() = externalCustomerIdProvider.getCustomerId().base58Value

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
            crashLogger.setCustomKey("country_from_moonpay", "not_set")
            return false
        }

        crashLogger.setCustomKey("is_moonpay_sell_enabled", ipFlags.isSellAllowed)
        crashLogger.setCustomKey("country_from_moonpay", ipFlags.currentCountryAbbreviation)

        return ipFlags.isSellAllowed
    }

    @Throws(MoonpaySellError::class)
    override suspend fun getUserSellTransactions(
        userAddress: Base58String,
    ): List<SellTransaction> = doMoonpayRequest {
        val userIdResponse = async { moonpayServerSideApi.getUserSellTransactions(externalCustomerId) }
        val externalIdResponse = async { moonpayServerSideApi.getUserSellTransactions(externalCustomerId) }
        val depositWallets = getDepositWalletsForTransactions(userIdResponse.await())

        mapper.fromNetwork(
            response = externalIdResponse.await(),
            depositWallets = depositWallets,
            selectedFiat = getSellFiatCurrency(),
            transactionOwnerAddress = userAddress,
        )
    }

    private suspend fun getDepositWalletsForTransactions(
        transactions: List<MoonpaySellTransactionShortResponse>
    ): List<MoonpaySellTransactionDepositWalletResponse> =
        transactions.filter { it.status == SellTransactionStatus.WAITING_FOR_DEPOSIT }
            .mapNotNull {
                kotlin.runCatching {
                    moonpayClientSideApi.getSellTransactionDepositWalletById(
                        transactionId = it.transactionId,
                        apiKey = BuildConfig.moonpayKey
                    )
                }.getOrNull() // skip if couldn't fetch
            }

    @Throws(MoonpaySellError::class)
    override suspend fun getSellQuoteForToken(
        tokenToSell: Token.Active,
        tokenAmount: BigDecimal,
        fiat: SellTransactionFiatCurrency
    ): MoonpaySellTokenQuote = doMoonpayRequest {
        mapper.fromNetwork(
            moonpayClientSideApi.getSellQuoteForToken(
                tokenSymbol = tokenToSell.tokenSymbol.lowercase(),
                apiKey = BuildConfig.moonpayKey,
                fiatName = fiat.abbriviation,
                tokenAmount = tokenAmount.toDouble()
            )
        )
    }

    override suspend fun getSellFiatCurrency(): SellTransactionFiatCurrency =
        cachedMoonpayIpFlags?.currentCountryAbbreviation.orEmpty()
            .let(SellTransactionFiatCurrency.Companion::getFromCountryAbbreviation)

    override suspend fun cancelSellTransaction(
        transactionId: String
    ): MoonpaySellCancelResult = withContext(dispatchers.io) {
        try {
            moonpayServerSideApi.cancelSellTransaction(transactionId)
            MoonpaySellCancelResult.CancelSuccess
        } catch (error: Throwable) {
            MoonpaySellCancelResult.CancelFailed(MoonpaySellError.UnknownError(error))
        }
    }

    @Throws(MoonpaySellError::class)
    private suspend inline fun <R> doMoonpayRequest(
        crossinline request: suspend CoroutineScope.() -> R
    ): R = withContext(dispatchers.io) {
        try {
            request.invoke(this)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Moonpay request failed")
            throw errorMapper.fromNetworkError(error)
        }
    }
}
