package org.p2p.wallet.sell.interactor

import org.p2p.core.utils.isNotZero
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.model.MoonpaySellTransaction
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlinx.coroutines.launch

private const val TAG = "SellInteractor"

class SellInteractor(
    private val sellRepository: MoonpaySellRepository,
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val homeLocalRepository: HomeLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val appScope: AppScope
) {

    private var isUserBalancePositive: Boolean = false
        set(value) {
            field = value
            Timber.tag(TAG).i("isUserBalancePositive updated to: $field")
        }

    suspend fun loadSellAvailability() {
        if (sellEnabledFeatureToggle.isFeatureEnabled) {
            sellRepository.loadMoonpayFlags()

            // observe for balance during the whole user session
            appScope.launch {
                homeLocalRepository.observeUserBalance()
                    .collect { isUserBalancePositive = it.isNotZero() }
            }
        }
    }

    fun isSellAvailable(): Boolean {
        return sellEnabledFeatureToggle.isFeatureEnabled &&
            sellRepository.isSellAllowedForUser() &&
            isUserBalancePositive
    }

    suspend fun loadUserSellTransactions(): List<MoonpaySellTransaction> {
        return sellRepository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
    }
}
