package org.p2p.wallet.settings.ui.network

import android.content.Context
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.main.repository.MainLocalRepository
import org.p2p.wallet.renbtc.service.RenVMService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.rpc.Environment
import timber.log.Timber

class SettingsNetworkPresenter(
    private val context: Context,
    private val mainLocalRepository: MainLocalRepository,
    private val environmentManager: EnvironmentManager
) : BasePresenter<SettingsNetworkContract.View>(), SettingsNetworkContract.Presenter {

    override fun setNewEnvironment(environment: Environment) {
        view?.showLoading(true)
        launch {
            try {
                environmentManager.saveEnvironment(environment)
                mainLocalRepository.clear()
                RenVMService.stopService(context)
                /* Sometimes these operations are completed too quickly
                 * On the UI it shows blinking loading effect which is not good
                 * Adding short delay to show loading state
                 * */
                delay(250L)
            } catch (e: Throwable) {
                Timber.e(e, "Error switching environment")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadData() {
        val environment = environmentManager.loadEnvironment()
        view?.showEnvironment(environment)
    }
}