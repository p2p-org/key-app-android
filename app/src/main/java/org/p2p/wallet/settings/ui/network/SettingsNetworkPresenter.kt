package org.p2p.wallet.settings.ui.network

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.renbtc.service.RenVMService
import timber.log.Timber

class SettingsNetworkPresenter(
    private val context: Context,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val mainLocalRepository: HomeLocalRepository,
    private val environmentManager: NetworkEnvironmentManager,
    private val analytics: BrowseAnalytics
) : BasePresenter<SettingsNetworkContract.View>(), SettingsNetworkContract.Presenter {

    private var currentNetworkName: String = environmentManager.loadCurrentEnvironment().name

    override fun setNewEnvironment(newNetwork: NetworkEnvironment) {
        launch {
            try {
                currentNetworkName = newNetwork.name
                environmentManager.chooseEnvironment(newNetwork)

                mainLocalRepository.clear()
                RenVMService.stopService(context)
                analytics.logNetworkChanging(currentNetworkName)
                // Sometimes these operations are completed too quickly
                // On the UI it shows blinking loading effect which is not good
                // Adding short delay to show loading state
                delay(250L)
            } catch (e: Throwable) {
                Timber.e(e, "Error switching environment")
            }
        }
    }

    override fun loadData() {
        view?.showEnvironment(
            currentNetwork = environmentManager.loadCurrentEnvironment(),
            isDevnetEnabled = inAppFeatureFlags.isDevNetEnabled.featureValue,
            availableNetworks = environmentManager.availableNetworks
        )
    }

    override fun save() {
        view?.onNetworkChanged(newName = currentNetworkName)
    }
}
