package org.p2p.wallet.home.events

import timber.log.Timber
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
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
            loadDetailsForStrigaAccounts()
            Timber.d("Striga accounts details loaded")
        }
    }

    private suspend fun loadDetailsForStrigaAccounts() = try {
        strigaWalletInteractor.getFiatAccountDetails()
        strigaWalletInteractor.getCryptoAccountDetails()
    } catch (e: Throwable) {
        Timber.e(e, "Unable to load striga accounts (fiat and crypto) details")
    }

    override suspend fun isEnabled(): Boolean {
        return strigaSignupEnabledFeatureToggle.isFeatureEnabled
    }
}
