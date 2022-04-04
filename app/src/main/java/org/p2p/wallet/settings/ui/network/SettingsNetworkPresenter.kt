package org.p2p.wallet.settings.ui.network

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.rpc.Environment
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.analytics.BrowseAnalytics
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.renbtc.service.RenVMService
import timber.log.Timber

class SettingsNetworkPresenter(
    private val context: Context,
    private val appFeatureFlags: AppFeatureFlags,
    private val mainLocalRepository: HomeLocalRepository,
    private val environmentManager: EnvironmentManager,
    private val analytics: BrowseAnalytics
) : BasePresenter<SettingsNetworkContract.View>(), SettingsNetworkContract.Presenter {
    private var networkName: String = environmentManager.loadEnvironment().name

    override fun setNewEnvironment(environment: Environment) {
        launch {
            try {
                networkName = environment.name
                environmentManager.saveEnvironment(environment)
                mainLocalRepository.clear()
                RenVMService.stopService(context)
                analytics.logNetworkChanging(networkName)
                /* Sometimes these operations are completed too quickly
                 * On the UI it shows blinking loading effect which is not good
                 * Adding short delay to show loading state
                 * */
                delay(250L)
            } catch (e: Throwable) {
                Timber.e(e, "Error switching environment")
            }
        }
    }

    override fun loadData() {
        val environment = environmentManager.loadEnvironment()
        view?.showEnvironment(environment, appFeatureFlags.isDevnetEnabled)
    }

    override fun save() {
        view?.onNetworkChanged(newName = networkName)
    }
}
