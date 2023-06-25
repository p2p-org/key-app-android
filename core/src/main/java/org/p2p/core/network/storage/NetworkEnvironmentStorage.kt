package org.p2p.core.network.storage

import org.p2p.core.network.environment.NetworkEnvironment

interface NetworkEnvironmentStorage {
    fun getCurrentEnvironment(): NetworkEnvironment?
    fun updateCurrentEnvironment(newEnvironment: NetworkEnvironment)
}
