package org.p2p.wallet.rpc.repository

import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager

class RpcRepository(private val environmentManager: EnvironmentManager) {

    init {
        environmentManager.setOnEnvironmentListener {

        }
    }
}