package org.p2p.ethereumkit

import org.koin.core.module.Module
import org.p2p.ethereumkit.external.EthereumModule
import org.p2p.ethereumkit.external.api.EthereumNetworkModule
import org.p2p.ethereumkit.internal.core.EthereumKit

object EthereumKitService {
    fun getEthereumKitModules(): List<Module> {
        return listOf(
            EthereumNetworkModule.create(),
            EthereumModule.create()
        ).also {
            EthereumKit.init()
        }
    }
}
