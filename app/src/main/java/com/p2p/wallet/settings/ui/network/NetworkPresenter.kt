package com.p2p.wallet.settings.ui.network

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.solanaj.rpc.Environment

class NetworkPresenter(
    private val environmentManager: EnvironmentManager
) : BasePresenter<NetworkContract.View>(), NetworkContract.Presenter {

    override fun setNewEnvironment(environment: Environment) {
        environmentManager.saveEnvironment(environment.endpoint)
    }

    override fun loadData() {
        val environment = environmentManager.loadEnvironment()
        view?.showEnvironment(environment)
    }
}