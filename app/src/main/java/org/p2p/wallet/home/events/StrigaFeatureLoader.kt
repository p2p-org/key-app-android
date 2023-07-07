package org.p2p.wallet.home.events

import org.p2p.wallet.common.feature_toggles.toggles.remote.TokenMetadataUpdateFeatureToggle
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails

class StrigaFeatureLoader(
    private val strigaSignupEnabledFeatureToggle: TokenMetadataUpdateFeatureToggle,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor
) : AppLoader {

    override suspend fun onLoad() {
        if (strigaSignupEnabledFeatureToggle.isFeatureEnabled) {
            strigaSignupInteractor.loadAndSaveSignupData()
            strigaUserInteractor.loadAndSaveUserStatusData()
            if (strigaUserInteractor.isUserCreated()) {
                loadStrigaFiatAccountDetails()
            }
        }
    }

    suspend fun loadStrigaFiatAccountDetails(): Result<StrigaFiatAccountDetails> {
        return strigaWalletInteractor.loadFiatAccountAndUserWallet()
    }
}
