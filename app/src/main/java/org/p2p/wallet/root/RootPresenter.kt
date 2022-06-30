package org.p2p.wallet.root

import org.p2p.solanaj.rpc.NetworkEnvironment
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.SettingsNetworkListFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class RootPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor,
    private val networkListFeatureToggle: SettingsNetworkListFeatureToggle,
    private val environmentManager: NetworkEnvironmentManager
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    override fun loadPricesAndBids() {
        launch {
            try {
                userInteractor.loadAllTokensData()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            }
        }
    }

    override fun loadAvailableNetworkEnvironments() {
        val networksFromRemoteConfig = networkListFeatureToggle.value.map { it.url }
        val isNetworkAvailable = { network: NetworkEnvironment -> network.endpoint in networksFromRemoteConfig }
        val availableEnvironments = NetworkEnvironment.values().filter(isNetworkAvailable)
        environmentManager.loadAvailableEnvironments(availableEnvironments)
    }

    override fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }
}
