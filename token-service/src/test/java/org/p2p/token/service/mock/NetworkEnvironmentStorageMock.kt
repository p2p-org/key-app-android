package org.p2p.token.service.mock

import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.storage.NetworkEnvironmentStorage

class NetworkEnvironmentStorageMock: NetworkEnvironmentStorage {
    private var currentEnvironment: NetworkEnvironment = NetworkEnvironment.RPC_POOL
    override fun getCurrentEnvironment(): NetworkEnvironment {
        return currentEnvironment
    }

    override fun updateCurrentEnvironment(newEnvironment: NetworkEnvironment) {
        currentEnvironment = newEnvironment
    }
}
