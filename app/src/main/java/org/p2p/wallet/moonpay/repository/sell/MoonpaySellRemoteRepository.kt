package org.p2p.wallet.moonpay.repository.sell

import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.scaleShort
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MoonpaySellRemoteRepository"

class MoonpaySellRemoteRepository(
    private val moonpayApi: MoonpayApi,
    private val sellFeatureToggle: SellEnabledFeatureToggle,
    private val moonpayApiKey: String,
    private val homeLocalRepository: HomeLocalRepository,
    private val crashLogger: CrashLogger,
    private val dispatchers: CoroutineDispatchers,
    appScope: AppScope
) : MoonpaySellRepository {

    private class MoonpayRepositoryInternalError(override val cause: Throwable) : Throwable(cause.message)

    // todo: maybe extract caching flags to a separate repository to reuse
    private var cachedMoonpayIpFlags: MoonpayIpAddressResponse? = null
    private var isUserBalancePositive: Boolean = false
        set(value) {
            field = value
            Timber.tag(TAG).i("isUserBalancePositive updated to: $field")
        }

    init {
        appScope.launch {
            homeLocalRepository.getTokensFlow().mapNotNull(::calculateTokenBalance)
                .catch { Timber.tag(TAG).e(MoonpayRepositoryInternalError(it)) }.collect { balance ->
                    isUserBalancePositive = balance.isMoreThan(BigDecimal.ZERO)
                }
        }
    }

    override suspend fun loadMoonpayFlags() {
        withContext(dispatchers.io) {
            try {
                cachedMoonpayIpFlags = moonpayApi.getIpAddress(moonpayApiKey)
                Timber.i("Moonpay IP flags were fetched successfully")
            } catch (e: Throwable) {
                Timber.e(MoonpayRepositoryInternalError(e))
            }
        }
    }

    override fun isSellAllowedForUser(): Boolean {
        if (!sellFeatureToggle.isFeatureEnabled) {
            return false
        }
        val ipFlags = cachedMoonpayIpFlags
        if (ipFlags == null) {
            Timber.e(MoonpayRepositoryInternalError(IllegalStateException("Moonpay IP flags were not fetched")))
            return false
        }

        val isSellAllowed = ipFlags.isSellAllowed &&
            sellFeatureToggle.isFeatureEnabled &&
            isUserBalancePositive

        crashLogger.setCustomKey("is_moonpay_sell_enabled", isSellAllowed)
        crashLogger.setCustomKey("country_from_moonpay", ipFlags.currentCountryAbbreviation)

        return isSellAllowed
    }

    private fun calculateTokenBalance(userTokens: List<Token.Active>): BigDecimal =
        userTokens.mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
}
