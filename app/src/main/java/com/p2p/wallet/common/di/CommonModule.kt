package com.p2p.wallet.common.di

import com.p2p.wallet.infrastructure.persistence.PreferenceService
import org.koin.dsl.module
import org.p2p.solanaj.rpc.RpcClient

object CommonModule : InjectionModule {

    override fun create() = module {
        single {
            val get = get<PreferenceService>()
            val selectedCluster = get.getSelectedCluster()
            RpcClient(selectedCluster)
        }
    }
}