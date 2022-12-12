package org.p2p.wallet.sell.interactor

import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository
import timber.log.Timber

private const val TAG = "SellInteractor"

class SellInteractor(
    private val sellRepository: MoonpaySellRepository,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val homeLocalRepository: HomeLocalRepository,
) {

    suspend fun loadSellAvailability() {
        if (sellEnabledFeatureToggle.isFeatureEnabled) {
            sellRepository.loadMoonpayFlags()
        }
    }

    suspend fun isSellAvailable(): Boolean {
        return sellEnabledFeatureToggle.isFeatureEnabled &&
            sellRepository.isSellAllowedForUser() &&
            isUserBalancePositive()
    }

    private suspend fun isUserBalancePositive(): Boolean {
        return try {
            homeLocalRepository.getUserBalance().isNotZero()
        } catch (error: Throwable) {
            Timber.tag(TAG).i(error, "Cant get user balance")
            false
        }
    }
}
