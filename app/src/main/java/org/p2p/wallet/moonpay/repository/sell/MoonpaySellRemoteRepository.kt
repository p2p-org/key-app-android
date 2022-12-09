package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import timber.log.Timber
import kotlinx.coroutines.withContext

private const val TAG = "MoonpaySellRemoteRepository"

class MoonpaySellRemoteRepository(
    private val moonpayApi: MoonpayApi,
    private val crashLogger: CrashLogger,
    private val dispatchers: CoroutineDispatchers,
) : MoonpaySellRepository {

    private class MoonpayRepositoryInternalError(override val cause: Throwable) : Throwable(cause.message)

    // todo: maybe extract caching flags to a separate repository to reuse
    private var cachedMoonpayIpFlags: MoonpayIpAddressResponse? = null

    override suspend fun loadMoonpayFlags() {
        withContext(dispatchers.io) {
            try {
                cachedMoonpayIpFlags = moonpayApi.getIpAddress(BuildConfig.moonpayKey)
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
}
