package org.p2p.wallet.home.ui.wallet.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.home.events.AppLoaderFacade
import org.p2p.wallet.home.events.StrigaFeatureLoader
import org.p2p.wallet.home.ui.wallet.model.WalletStrigaOnOffRampTokens
import org.p2p.wallet.striga.offramp.interactor.StrigaOffRampInteractor
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

/**
 * Intermediate singleton repository which collects data to show in on/off ramp cells
 * and emits them to StateFlow [observeTokens]
 */
class WalletStrigaOnOffRampTokensRepository(
    private val dispatchers: CoroutineDispatchers,
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val appLoaderFacade: AppLoaderFacade,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaOffRampInteractor: StrigaOffRampInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
) {
    private val rampTokensFlow = MutableStateFlow(WalletStrigaOnOffRampTokens())
    private val cannotLoadTokens: Boolean
        get() = !strigaSignupEnabledFeatureToggle.isFeatureEnabled || !strigaUserInteractor.canLoadAccounts

    fun observeTokens(): StateFlow<WalletStrigaOnOffRampTokens> = rampTokensFlow.asStateFlow()

    suspend fun load() = withContext(dispatchers.io) {
        // synchronization point: striga loads data after pin entered and response can be delayed
        appLoaderFacade.await(StrigaFeatureLoader::class.java)

        if (cannotLoadTokens) {
            rampTokensFlow.emit(WalletStrigaOnOffRampTokens())
            return@withContext
        }

        strigaWalletInteractor.loadDetailsForStrigaAccounts(ignoreCache = true)
        val strigaOnRampTokens = strigaOnRampInteractor.getOnRampTokens().successOrNull().orEmpty()
        val strigaOffRampTokens = strigaOffRampInteractor.getOffRampTokens().successOrNull().orEmpty()

        rampTokensFlow.emit(
            WalletStrigaOnOffRampTokens(
                offRampTokens = strigaOffRampTokens,
                onRampTokens = strigaOnRampTokens,
            )
        )
    }
}
