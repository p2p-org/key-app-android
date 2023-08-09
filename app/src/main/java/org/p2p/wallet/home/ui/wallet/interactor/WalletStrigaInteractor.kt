package org.p2p.wallet.home.ui.wallet.interactor

import kotlinx.coroutines.flow.StateFlow
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.home.events.AppLoaderFacade
import org.p2p.wallet.home.events.StrigaFeatureLoader
import org.p2p.wallet.home.ui.wallet.mapper.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.offramp.interactor.StrigaOffRampInteractor
import org.p2p.wallet.striga.offramp.models.StrigaOffRampToken
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampInteractor
import org.p2p.wallet.striga.onramp.interactor.StrigaOnRampToken
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

data class WalletStrigaOnOffRampTokens(
    val offRampTokens: List<StrigaOffRampToken>,
    val onRampTokens: List<StrigaOnRampToken>,
)

class WalletStrigaInteractor(
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val appLoaderFacade: AppLoaderFacade,
    private val strigaWalletInteractor: StrigaWalletInteractor,
    private val strigaOnRampInteractor: StrigaOnRampInteractor,
    private val strigaOffRampInteractor: StrigaOffRampInteractor,
    private val strigaUserInteractor: StrigaUserInteractor,
) {

    suspend fun getOnOffRampTokens(): WalletStrigaOnOffRampTokens {
        // synchronization point: striga loads data after pin entered and response can be delayed
        appLoaderFacade.await(StrigaFeatureLoader::class.java)
        if (!strigaSignupEnabledFeatureToggle.isFeatureEnabled || !strigaUserInteractor.canLoadAccounts) {
            return WalletStrigaOnOffRampTokens(emptyList(), emptyList())
        }

        strigaWalletInteractor.loadDetailsForStrigaAccounts(ignoreCache = true)
        val strigaOnRampTokens = strigaOnRampInteractor.getOnRampTokens().successOrNull().orEmpty()
        val strigaOffRampTokens = strigaOffRampInteractor.getOffRampTokens().successOrNull().orEmpty()

        return WalletStrigaOnOffRampTokens(
            offRampTokens = strigaOffRampTokens,
            onRampTokens = strigaOnRampTokens,
        )
    }

    suspend fun observeStrigaKycBanner(): StateFlow<StrigaKycStatusBanner?> =
        strigaUserInteractor.getUserStatusBannerFlow()
}
