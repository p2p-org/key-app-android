package org.p2p.wallet.home.events

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.toggles.remote.StrigaSignupEnabledFeatureToggle
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaBannersLoader(
    private val strigaUserInteractor: StrigaUserInteractor,
    private val strigaSignupEnabledFeatureToggle: StrigaSignupEnabledFeatureToggle,
    private val appScope: AppScope,
    private val homeInteractor: HomeInteractor
) : AppLoader {

    override suspend fun onLoad() {
        if (!strigaSignupEnabledFeatureToggle.isFeatureEnabled) {
            return
        }
        appScope.launch {
            strigaUserInteractor.getUserStatusBannerFlow()
                .filterNotNull()
                .collect {
                    homeInteractor.updateStrigaKycBanner(it)
                }
        }
    }
}
