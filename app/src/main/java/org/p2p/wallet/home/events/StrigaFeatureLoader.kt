package org.p2p.wallet.home.events

import timber.log.Timber
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaFeatureLoader(
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupInteractor: StrigaSignupInteractor,
) : AppLoader() {

    override suspend fun onLoad() {
        strigaSignupInteractor.loadAndSaveSignupData()
        Timber.d("Striga signup data loaded")
        strigaUserInteractor.loadAndSaveUserStatusData()
        Timber.i("Striga user status loaded (is KYC approved? ${strigaUserInteractor.isKycApproved})")
    }

    override suspend fun isEnabled(): Boolean {
        return strigaSignupEnabledFeatureToggle.isFeatureEnabled
    }
}
