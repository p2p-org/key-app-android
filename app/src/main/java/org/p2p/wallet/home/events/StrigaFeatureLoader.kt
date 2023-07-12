package org.p2p.wallet.home.events

import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails

class StrigaFeatureLoader(
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor
) : AppLoader {

    override suspend fun onLoad() {
        strigaSignupInteractor.loadAndSaveSignupData()
        strigaUserInteractor.loadAndSaveUserStatusData()
        if (strigaUserInteractor.isUserCreated()) {
            loadStrigaFiatAccountDetails()
        }
    }

    suspend fun loadStrigaFiatAccountDetails(): Result<StrigaFiatAccountDetails> {
        return strigaWalletInteractor.loadFiatAccountAndUserWallet()
    }

    override suspend fun isEnabled(): Boolean {
        return strigaSignupEnabledFeatureToggle.isFeatureEnabled
    }
}
