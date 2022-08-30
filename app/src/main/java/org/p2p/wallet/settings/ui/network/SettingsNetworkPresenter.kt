package org.p2p.wallet.settings.ui.network

import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import timber.log.Timber

class SettingsNetworkPresenter(
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val environmentManager: NetworkEnvironmentManager,
) : BasePresenter<SettingsNetworkContract.View>(), SettingsNetworkContract.Presenter {

    private var currentSelectedEnvironment: NetworkEnvironment = environmentManager.loadCurrentEnvironment()

    override fun attach(view: SettingsNetworkContract.View) {
        super.attach(view)
        loadEnvironments()
    }

    private fun loadEnvironments() {
        Timber.d(environmentManager.availableNetworks.toString())
        view?.showEnvironment(
            currentNetwork = currentSelectedEnvironment,
            isDevnetEnabled = inAppFeatureFlags.isDevNetEnabled.featureValue,
            availableNetworks = environmentManager.availableNetworks
        )
    }

    override fun onNewEnvironmentSelected(newNetwork: NetworkEnvironment) {
        currentSelectedEnvironment = newNetwork
    }

    override fun confirmNetworkEnvironmentSelected() {
        if (currentSelectedEnvironment == environmentManager.loadCurrentEnvironment()) {
            view?.close()
        } else {
            view?.closeWithResult(currentSelectedEnvironment)
        }
    }
}
