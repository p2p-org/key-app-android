package org.p2p.wallet.moonpay.repository.sell

import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayIpAddressResponse
import org.p2p.wallet.utils.isMoreThan
import org.p2p.wallet.utils.scaleShort
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext

class MoonpaySellRemoteRepository(
    private val moonpayApi: MoonpayApi,
    private val sellFeatureToggle: SellEnabledFeatureToggle,
    private val moonpayApiKey: String,
    private val homeLocalRepository: HomeLocalRepository,
    private val dispatchers: CoroutineDispatchers
) : MoonpaySellRepository {

    // todo: maybe extract caching flags to a separate repository to reuse
    private var cachedMoonpayIpFlags: MoonpayIpAddressResponse? = null

    override suspend fun loadMoonpayFlags() {
        withContext(dispatchers.io) {
            try {
                cachedMoonpayIpFlags = moonpayApi.getIpAddress(moonpayApiKey)
                Timber.i("Moonpay IP flags were fetched successfully")
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }
    }

    override suspend fun isSellAllowedForUser(): Boolean {
        if (!sellFeatureToggle.isFeatureEnabled) {
            return false
        }
        val ipFlags = cachedMoonpayIpFlags
        if (ipFlags == null) {
            Timber.e(IllegalStateException("Moonpay IP flags were not fetched"))
            return false
        }

        return ipFlags.isSellAllowed &&
            sellFeatureToggle.isFeatureEnabled &&
            isUserBalancePositive()
    }

    private suspend fun isUserBalancePositive(): Boolean =
        homeLocalRepository.getUserTokens()
            .mapNotNull(Token.Active::totalInUsd)
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .scaleShort()
            .isMoreThan(BigDecimal.ZERO)
}
