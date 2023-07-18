package org.p2p.wallet.home.events

import timber.log.Timber
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

class StrigaFeatureLoader(
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
    private val strigaWalletInteractor: StrigaWalletInteractor
) : AppLoader {

    override suspend fun onLoad() {
        strigaSignupInteractor.loadAndSaveSignupData()
        Timber.d("Striga signup data loaded")
        strigaUserInteractor.loadAndSaveUserStatusData()
        Timber.d("Striga user status loaded (is KYC approved? ${strigaUserInteractor.isKycApproved})")
        if (strigaUserInteractor.canLoadAccounts) {
            strigaWalletInteractor.loadDetailsForStrigaAccounts()
            Timber.d("Striga accounts details loaded")
        }
    }

    override suspend fun isEnabled(): Boolean {
        return strigaSignupEnabledFeatureToggle.isFeatureEnabled
    }
}
