package com.p2p.wallet.settings.ui.network

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import com.p2p.wallet.main.repository.MainLocalRepository
import com.p2p.wallet.main.repository.RenBTCRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.solanaj.rpc.Environment
import timber.log.Timber

class NetworkPresenter(
    private val renBTCRepository: RenBTCRepository,
    private val mainLocalRepository: MainLocalRepository,
    private val environmentManager: EnvironmentManager
) : BasePresenter<NetworkContract.View>(), NetworkContract.Presenter {

    override fun setNewEnvironment(environment: Environment) {
        view?.showLoading(true)
        launch {
            try {
                environmentManager.saveEnvironment(environment)
                renBTCRepository.clearSessionData()
                mainLocalRepository.clear()
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